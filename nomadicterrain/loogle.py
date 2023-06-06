"""
Loogle - Your Local Google

Index and search your local hard drive, a pure python replacement for
recoll.

Indexes all pdf, djvu, txt, epub files under a given directory, saves
the index, and allows search on these indexed documents. 

Point to any directory, specify an index database name, loogle will
index all books, documents for you. Content extraction is done through
`textract` which can extract files from pretty much all common file
formats. Loogle can detect additions, removals.

TODO: file size changes triggering incremental updates.

Requirements

Python packages

`textract`
`sqlite3`

Programs

`pdftotext`
`djvutxt`

Install with `apt-get install djulibre-bin  poppler-utils`
"""
import os, io, codecs, sqlite3, json, sys

exts = ['.pdf']
skip_dir = 'kitaplar/General/novel'
escapes = ''.join([chr(char) for char in range(1, 32)])

if os.path.isdir("/tmp"): os.environ['TMPDIR'] = "/tmp"

def getstatusoutput(cmd):
    """Return (status, output) of executing cmd in a shell."""
    pipe = os.popen(cmd + ' 2>&1', 'r')
    text = pipe.read()
    sts = pipe.close()
    if sts is None: sts = 0
    if text[-1:] == '\n': text = text[:-1]
    return sts, text

def deleteFile(path):
    """deletes the path entirely"""
    print (path)
    mswindows = (sys.platform == "win32")
    if mswindows: 
        cmd = 'DEL /F /S /Q "%s"' % path
    else:
        cmd = 'rm -rf "' + path + '"'
    result = getstatusoutput(cmd)
    if(result[0]!=0):
        raise RuntimeError(result[1])

def ls(d,ignore_list=[]):
    print ('ls ignore lst', ignore_list)
    dirs = []; files = []
    for root, directories, filenames in os.walk(d):
        for directory in directories:
            path = os.path.join(root, directory)
            do_add = True
            for ignore in ignore_list:
                if ignore in path:
                    print ('ignoring', path); do_add = False
            if do_add: dirs.append(path)
        for filename in filenames: 
            path = os.path.join(root,filename)
            do_add = True
            for ignore in ignore_list:
                if ignore in path: do_add = False
            if do_add: files.append((path, os.path.getsize(path)))
    return dirs, files

def process(file):
    import textract
    if ".pdf" in file:
        os.system("pdftotext '%s' %s/out.txt" % (file,os.environ['TMPDIR']))
        res = codecs.open(os.environ['TMPDIR'] + "/out.txt", encoding="utf-8").read()
        return res
    elif ".djvu" in file: 
        os.system("djvutxt '%s' %s/out.txt" % (file,os.environ['TMPDIR']))
        res = codecs.open(os.environ['TMPDIR'] + "/out.txt", encoding="utf-8").read()
        return res
    else:
        textract.process(file, encoding='ascii').decode('utf-8')      

def get_legit_files(crawl_dir):
    dirs, files = ls(crawl_dir)
    files = [(f,size) for (f,size) in files if os.path.splitext(f)[1] in exts]
    files = [x for x in files if skip_dir not in x[0]]    
    return files
    
def get_existing_paths(conn):
    c = conn.cursor()    
    c.execute('''SELECT path FROM BOOKS;''')
    rows = c.fetchall()
    rows = dict((x[0],"1") for x in rows)
    return rows
    
def index(crawl_dir,index_db,new_index=False):
    files = get_legit_files(crawl_dir)
    conn = None
    if new_index:
        if os.path.isfile(index_db):
            deleteFile(index_db)
        conn = sqlite3.connect(index_db)
        c = conn.cursor()
        c.execute('''CREATE VIRTUAL TABLE BOOKS USING fts3(path TEXT PRIMARY KEY, content TEXT, size TEXT); ''')
        c.close()
    else:
        conn = sqlite3.connect(index_db)
        
    c = conn.cursor()

    existing_paths = get_existing_paths(conn)
    print ("crawl",crawl_dir)
    for i,(file,size) in enumerate(files):
        rel_file = file.replace(crawl_dir,"")
        if rel_file in existing_paths:
            print (rel_file, "already there")
            continue
        print ("Rel",rel_file)
        print ('Indexing ', file)
        filename_as_content = os.path.basename(file).replace("_"," ").replace("-"," ")
        filename_as_content = filename_as_content[0:filename_as_content.rfind(".")]
        content = filename_as_content
        try:
            content += " " + process(file)
        except Exception as e:
            print ("Error")
            print ("Indexing only ", content)
        content = content.replace("'","").replace("\x00", "")
        c.execute('''INSERT INTO BOOKS(path,content,size) VALUES('%s','%s','%s'); ''' % (rel_file,content,size))
        conn.commit()
        # now do the delete
    delete(crawl_dir,index_db)
            
def delete(crawl_dir,index_db):    
    files = get_legit_files(crawl_dir)
    files = [x[0].replace(crawl_dir,"") for x in files]
    conn = sqlite3.connect(index_db)
    c = conn.cursor()
    c.execute('''SELECT path,size FROM BOOKS;''')
    rows = c.fetchall()
    for r in rows:
        if r[0] not in files:
            print (r[0], "deleting..")
            c.execute('''DELETE FROM BOOKS WHERE path = '%s';''' % r[0])
                        
    conn.commit()
    conn.close()

def search(s, index_db):
    print (s)
    conn = sqlite3.connect(index_db)
    c = conn.cursor()
    c.execute('''SELECT path, snippet(BOOKS) FROM BOOKS WHERE content MATCH "%s";''' % s)
    res = c.fetchall()
    return res

def index_nomadic():
    params = json.loads(open(os.environ['HOME'] + "/.nomterr.conf").read())
    #index(params['book_dir'], params['book_index_db'],new_index=True)
    index(params['book_dir'], params['book_index_db'])

def test_search():
    params = json.loads(open(os.environ['HOME'] + "/.nomterr.conf").read())
    res = search("wing span euler bernoulli", params['book_index_db'])
    for path,summary in res:
        summary = summary.replace("<b>","")
        summary = summary.replace("</b>","")
        print (path, summary)
    
if __name__ == "__main__": 
 
    index_nomadic()

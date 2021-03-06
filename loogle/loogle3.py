import sys; sys.path.append('..')
import os, io, codecs, rsync
import sqlite3, textract

exts = ['.pdf','.djvu','.txt','.html','epub','mobi']
skip_dir = 'kitaplar/General/novel'
escapes = ''.join([chr(char) for char in range(1, 32)])

if os.path.isdir("/tmp"): os.environ['TMPDIR'] = "/tmp"

def process(file):
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
    dirs, files = rsync.ls(crawl_dir)
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
            rsync.deleteFile(index_db)
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
            #print ("Already there")
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
    c.execute('''SELECT path FROM BOOKS WHERE content MATCH "%s";''' % s)
    res = c.fetchall()
    return res


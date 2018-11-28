import sys; sys.path.append('..')
import os, shutil, io, codecs, textract, rsync
import pandas as pd, sqlite3

exts = ['.pdf','.djvu','.txt','.html','epub','mobi']
skip_dir = 'kitaplar/General/novel'

def get_legit_files(crawl_dir):
    dirs, files = rsync.ls(crawl_dir)
    files = [(f,size) for (f,size) in files if os.path.splitext(f)[1] in exts]
    files = [x for x in files if skip_dir not in x[0]]    
    return files

def row_exists(conn, path):
    c = conn.cursor()    
    c.execute('''SELECT count(*) FROM BOOKS where path = '%s' ''' % path)
    rows = c.fetchall()
    return rows[0][0]==1
    
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
    for i,(file,size) in enumerate(files):
        print ('Indexing ', file)
        if row_exists(conn, file):
            print ("Already there")
            continue
        filename_as_content = os.path.basename(file).replace("_"," ").replace("-"," ")
        filename_as_content = filename_as_content[0:filename_as_content.rfind(".")]
        print ("filename_as_content", filename_as_content)
        content = filename_as_content
        try:
            content += " " + textract.process(file, encoding='ascii').decode('utf-8')
            print (content)
            c.execute('''INSERT INTO BOOKS(path,content,size) VALUES('%s','%s','%s'); ''' % (file,content,size))
            conn.commit()
        except Exception as e:
            print ("Error", repr(e))
            print ("Indexing only ", content)
            
def delete(crawl_dir,index_db):    
    files = get_legit_files(crawl_dir)
    files = [x[0] for x in files]
    conn = sqlite3.connect(index_db)
    c = conn.cursor()
    c.execute('''SELECT path,size FROM BOOKS;''')
    rows = c.fetchall()
    for r in rows:
        if r[0] not in files:
            c.execute('''DELETE FROM BOOKS WHERE path = '%s';''' % r[0])
            print (r[0], "deleted")
                        
    conn.commit()
    conn.close()

def search(s, index_db):
    print (s)
    conn = sqlite3.connect(index_db)
    c = conn.cursor()
    c.execute('''SELECT path FROM BOOKS WHERE content MATCH "%s";''' % s)
    res = c.fetchall()
    return res


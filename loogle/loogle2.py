import sys; sys.path.append('..')
from whoosh.index import create_in 
from whoosh.fields import Schema
from whoosh.query import Term, Or
from whoosh.index import open_dir 
from whoosh.fields import ID, KEYWORD, TEXT 
from whoosh.qparser import QueryParser 
import os, shutil, io, codecs, textract, rsync

def index(crawl_dir,index_dir,new_index=False):
    
    if new_index: 
        if os.path.isdir(index_dir): rsync.deleteDir(index_dir)
        os.mkdir(index_dir)
    
    pdf_schema = Schema(path = ID(stored=True),  
                        title = TEXT(stored=True), 
                        text = TEXT) 
    index = create_in(index_dir, pdf_schema)

    writer = index.writer() 

    dirs, files = rsync.ls(crawl_dir)
    print (files)
    for i,(file,size) in enumerate(files):
        print ('Indexing ', file)
        content = textract.process(file,encoding='ascii')
        filename_as_content = os.path.basename(file).replace("_"," ").replace("-"," ")
        filename_as_content = filename_as_content.encode('utf-8')
        print (filename_as_content)
        print (file)
        print (content.decode('utf-8'))
        print ('----------------------')
        writer.add_document(path = str(file),
                            title = str(filename_as_content),
                            text = content.decode('utf-8'))
    writer.commit() 

def search(s, index_dir):
    index = open_dir(index_dir) 
    searcher = index.searcher() 
    parser = QueryParser("text", index.schema) 
    query = parser.parse(s)
    query = Or([query, Term("title", s)]) 
    results = searcher.search(query)
    res = [x['path'] for x in results]
    return res

index_dir = "/tmp/idx"
    
index("/home/burak/Documents/loogle/sub", index_dir, new_index=True)

res = search("scientist", index_dir)

print (res)

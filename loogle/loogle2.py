import sys; sys.path.append('..')
import pandas as pd
from whoosh.index import create_in 
from whoosh.fields import Schema
from whoosh.query import Term, Or
from whoosh.index import open_dir 
from whoosh.fields import ID, KEYWORD, TEXT 
from whoosh.qparser import QueryParser 
import os, shutil, io, codecs, textract, rsync

def index(crawl_dir,index_dir,new_index=False):

    dirs, files = rsync.ls(crawl_dir)
    file_names = [f[0] for f in files]
    print ('files', files)
    
    if new_index:
        if os.path.isdir(index_dir): rsync.deleteDir(index_dir)
        os.mkdir(index_dir)
        pdf_schema = Schema(path = ID(stored=True),  
                            title = TEXT(stored=True), 
                            text = TEXT) 
        index = create_in(index_dir, pdf_schema)
        file_df = pd.DataFrame(columns=['file','size'])
    else:
        # existing index
        file_df = pd.read_csv(index_dir + "/files.csv",sep='|')
        index = open_dir(index_dir)
                
    writer = index.writer()

    # check for deletions first
    print ('deletions')
    print ('file names', file_names)
    for f in list(file_df.file):
        if f not in file_names: print (f, 'deleted')
        writer.delete_by_term('path', f)    
        
    for i,(file,size) in enumerate(files):
        if file not in list(file_df.file): 
            print ('Indexing ', file)
            content = textract.process(file,encoding='ascii')
            filename_as_content = os.path.basename(file).replace("_"," ").replace("-"," ")
            filename_as_content = filename_as_content.encode('utf-8')
            writer.add_document(path = str(file),
                                title = str(filename_as_content),
                                text = content.decode('utf-8'))
            file_df = file_df.append({"file": file, "size": size},ignore_index=True)
        else:
            print ("already there")
            
    writer.commit()
    file_df.to_csv(index_dir + "/files.csv",sep='|',index=None) 

def search(s, index_dir):
    index = open_dir(index_dir) 
    searcher = index.searcher() 
    parser = QueryParser("text", index.schema) 
    query = parser.parse(s)
    query = Or([query, Term("title", s)]) 
    results = searcher.search(query)
    res = [x['path'] for x in results]
    return res

if __name__ == "__main__": 
 
    index_dir = "/tmp/idx"    
    index("/home/burak/Documents/kod/loogle/sub", index_dir, new_index=True)
    index("/home/burak/Documents/kod/loogle/sub", index_dir)
    res = search("scientist", index_dir)
    print (res)

import sys; sys.path.append('..')
import pandas as pd
from whoosh.index import create_in 
from whoosh.fields import Schema
from whoosh.query import Term, Or
from whoosh.index import open_dir 
from whoosh.fields import ID, KEYWORD, TEXT 
from whoosh.qparser import QueryParser
from whoosh.filedb.filestore import FileStorage
import os, shutil, io, codecs, textract, rsync

exts = ['.pdf','.djvu','.txt','.html','epub','mobi']

skip_dir = 'kitaplar/General/novel'

def index(crawl_dir,index_dir,new_index=False, stop_after_n=100):

    dirs, files = rsync.ls(crawl_dir)
    files = [(f,size) for (f,size) in files if os.path.splitext(f)[1] in exts]
    files = [x for x in files if skip_dir not in x[0]]
    
    file_names = [f[0] for f in files]
    #print ('files', files)
    index = None
    if new_index:
        print ('new index------------------------')
        if os.path.isdir(index_dir): rsync.deleteDir(index_dir)
        os.mkdir(index_dir)
        pdf_schema = Schema(path = ID(unique=True, stored=True),  
                            title = TEXT(stored=True), 
                            text = TEXT)             
        index = create_in(index_dir, pdf_schema)
        file_df = pd.DataFrame(columns=['file','size'])
    else:
        # existing index
        print ('existing index------------------------')
        file_df = pd.read_csv(index_dir + "/files.csv",sep='|')
        storage = FileStorage(index_dir)
        index = storage.open_index()

    print ("doc count 1", index.doc_count(), index.doc_count_all())

    writer = index.writer()
    
    # check for deletions first
    print ('deletions')
    #print ('file names', file_names)
    for f in list(file_df.file):
        if f not in file_names: print (f, 'deleted')        
        writer.delete_by_term('path', f)    
        
    for i,(file,size) in enumerate(files):
        if i % 10==0: print (i)
        if i==stop_after_n:
            print ('Stopping after', i, 'files')
            break
        if file not in list(file_df.file): 
            print ('Indexing ', file)
            filename_as_content = os.path.basename(file).replace("_"," ").replace("-"," ")
            filename_as_content = filename_as_content[0:filename_as_content.rfind(".")]
            print ("filename_as_content", filename_as_content)
            content = filename_as_content
            try:
                content += " " + textract.process(file, encoding='ascii').decode('utf-8')
                print (content)
            except Exception as e:
                print ("Error", repr(e))
                print ("Indexing only ", content)                    
            writer.add_document(path = str(file),
                                title = str(filename_as_content),
                                text = content)
            #writer.commit()
            file_df = file_df.append({"file": file, "size": size},ignore_index=True)
        else:
            print ("already there")
            
    writer.commit()

    print ("doc count 2", index.doc_count(), index.doc_count_all(), index._segments())
    
    index.close()

    file_df.to_csv(index_dir + "/files.csv",sep='|',index=None) 

def search(s, index_dir):
    storage = FileStorage(index_dir)
    index = storage.open_index()
    searcher = index.searcher() 
    parser = QueryParser("text", index.schema) 
    query = parser.parse(s)
    query = Or([query, Term("title", s)]) 
    results = searcher.search(query)
    res = [x['path'] for x in results]
    searcher.close()
    return res

if __name__ == "__main__": 
 
    index_dir = "/tmp/idx"    
    index("/home/burak/Documents/kod/loogle/sub", index_dir, new_index=True)
    index("/home/burak/Documents/kod/loogle/sub", index_dir)
    res = search("scientist", index_dir)
    print (res)

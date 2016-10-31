# Your Local Google - index and search your local hard drive
#
# Indexes all pdf,djvu,txt,epub files under a given directory [tbd
# epub], saves the index, and allows search on these indexed
# documents. Allows updates, and deletes of files already indexed. 
#
# Requirements
# whoosh, pandas, chardet, shutil, rsync (under same repo),
# webarticle2text (same repo)
# pdftotext.exe - this is bundled with tetex for windows
# djvutotext - https://sourceforge.net/projects/djvu/files/
# unix2dos - for a charset conversion, something else could be used here
#

import os, sys, pandas as pd; sys.path.append('c:/Users/burak/Documents/kod')
from whoosh.index import create_in 
from whoosh.fields import Schema
from whoosh.query import Term, Or
from whoosh.index import open_dir 
from whoosh.fields import ID, KEYWORD, TEXT 
from whoosh.qparser import QueryParser 
import rsync, shutil, io, codecs
import webarticle2text

exts = ['.pdf','.djvu','.txt','.html']

pdfcmd = 'pdftotext -enc UTF-8 "%s" %s/loog.txt'
djvucmd = 'djvutxt.exe "%s" %s/loog.txt'
           
def index(crawl_dir,index_dir,get_first_N=None):

    df_files = []; file_indexed_dict = {} ; index = None
    if os.path.isfile(index_dir + "/loogle_files.csv" ):
        df = pd.read_csv(index_dir + "/loogle_files.csv")
        df_files = df[['file','size']].values.tolist()
        file_indexed_dict = df.set_index('file').to_dict()['size']
        index = open_dir(index_dir)
    else:     
        pdf_schema = Schema(path = ID(stored=True),  
                            title = TEXT(stored=True), 
                            text = TEXT) 
        index = create_in(index_dir, pdf_schema)
        
    writer = index.writer() 

    # get all potential files to be indexed
    dirs, files = rsync.ls(crawl_dir)
    files = [(f.replace("\\","/"),size) for (f,size) in files ]
    files = [(f,size) for (f,size) in files if os.path.splitext(f)[1] in exts]

    files_crawled_dict = dict(files)
    tmp = {} # needed bcz cannot change file_indexed_dict while iterating
    for ff in file_indexed_dict:
        # remove file from index if file exists in index, but not on
        # file system
        if ff not in files_crawled_dict:
            print ff, 'removed'
            writer.delete_by_term('path', unicode(ff))
            tmp[ff] = file_indexed_dict[ff]
        elif files_crawled_dict[ff] != file_indexed_dict[ff]:
            # this is the only section we do not add to tmp this is
            # how I remove an updated file from my index dictionary so
            # its absence will be detected below, and will be freshly
            # reindexed
            print ff, 'size different update'
            writer.delete_by_term('path', unicode(ff))
        else:
            tmp[ff] = file_indexed_dict[ff]

    # put it back in
    file_indexed_dict = tmp
                
    if get_first_N: files = files[:get_first_N]
    for i,(file,size) in enumerate(files):
        try:
            if file in file_indexed_dict:
                print 'skipping', file
                continue
            print 'processing', file
            ext = os.path.splitext(file)[1]
            if ext == ".pdf" :
                cmd = pdfcmd % (file,os.environ['TEMP'])
                os.system(cmd)
            elif ext == ".djvu":
                cmd = djvucmd % (file,os.environ['TEMP'])
                os.system(cmd)
                os.system("unix2dos -7 %s/loog.txt" % os.environ['TEMP'])
            elif ext == ".html":
                with codecs.open(file, encoding='utf-8') as f:
                    content = f.read()
                content = webarticle2text.extractFromHTML(content)
                fout = open("%s/loog.txt" % os.environ['TEMP'],"w")
                fout.write(content.encode("utf8"))
                fout.close()            
            elif ext == ".txt":
                shutil.copy(file, "%s/loog.txt" % os.environ['TEMP'])


            # turn the file name itself into content as well just in case,
            # if text conversion does not output anything, at least we can
            # use some information from the file name for search
            filename_as_content = os.path.basename(file).replace("_"," ").replace("-"," ")
            filename_as_content = filename_as_content.decode("latin-1")
            for x in exts: filename_as_content = filename_as_content.replace(x,"")
            filename_as_content += " "

            with codecs.open("%s/loog.txt" % os.environ['TEMP'], encoding='utf-8') as f:
                content = f.read()
            writer.add_document(path = unicode(file),
                                title = unicode(filename_as_content),
                                text = unicode(content))
            df_files.append([file, size])
            
        except:
            print 'error detected', e
            continue
    writer.commit() 
    df_files = pd.DataFrame(df_files,columns=['file','size'])
    df_files.to_csv(index_dir + "/loogle_files.csv",index=None)
    
def search(s, index_dir):
    index = open_dir(index_dir) 
    searcher = index.searcher() 
    parser = QueryParser("text", index.schema) 
    query = parser.parse(s)
    # search both in the text _or_ the title
    # some books, especially djvu ones, do not have any
    # content in them, in that case getting a hit on the
    # title is the last chance
    query = Or([query, Term("title", s)]) 
    results = searcher.search(query)
    res = [x['path'] for x in results]
    return res
    
if __name__ == "__main__":

    index_dir = "c:/Users/burak/Downloads/book_idx"
    flip_drive = False
    
    if len(sys.argv) == 4 and sys.argv[3] == "de": flip_drive = True
    if sys.argv[1] == '--index':
        index(crawl_dir="d:/kitaplar",
              index_dir=index_dir,
              get_first_N=2500) # up this for incremental processing
        
    if sys.argv[1] == '--find': 
        res = search(sys.argv[2], index_dir=index_dir)
        for x in res:
            # produce emacs friendly output here, file:line_no:content
            # allows emacs find-grep to make the output clickable, C-c C-c
            # will take you to the file
            if not flip_drive: print "%s:1:-" % x
            else: print "%s:1:-" % x.replace("d:","e:/archive")
        

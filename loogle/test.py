import sys; sys.path.append('..')
import loogle3, rsync

index_db = "/home/burak/Downloads/test.db"
cdir = "/home/burak/Documents/kod/loogle/sub"

def test_simple():
   loogle3.index(cdir, index_db, new_index=True)
   res = loogle3.search("scientist", index_db)
   print (res)

def test_incr():
   loogle3.index(cdir, index_db, new_index=True)
   loogle3.index(cdir, index_db)
   res = loogle3.search("scientist", index_db)
   print (res)

def test_add_new():
    loogle3.index(cdir, index_db, new_index=True)
    res = loogle3.search("doctor", index_db)
    print ("doctor search 1", res)
    res = loogle3.search("scientist", index_db)
    print ("scientist search 1", res)

    # create new 'doc'
    fout = open(cdir + "/new_book.txt","w")
    fout.write("doctor something something blabla scientist")
    fout.close()
    
    loogle3.index(cdir, index_db, new_index=False)
    res = loogle3.search("doctor", index_db)
    print ("doctor search 2", res)
    res = loogle3.search("scientist", index_db)
    print ("scientist search 2", res)
    rsync.deleteFile(cdir + "/new_book.txt")

def test_delete():
    loogle3.index(cdir, index_db, new_index=True)
    fout = open(cdir + "/new_book.txt","w")
    fout.write("doctor something something blabla scientist")
    fout.close()
    loogle3.index(cdir, index_db)
    res = loogle3.search("doctor", index_db)
    print (res)
    res = loogle3.search("scientist", index_db)
    print (res)
    rsync.deleteFile(cdir + "/new_book.txt")
    loogle3.delete(cdir, index_db)
    res = loogle3.search("doctor", index_db)
    print (res)
    
if __name__ == "__main__":
    test_simple()
    test_incr()
    test_add_new()
    test_delete()

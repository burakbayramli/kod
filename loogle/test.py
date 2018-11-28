import sys; sys.path.append('..')
import loogle2, rsync

index_dir = "/tmp/idx"
cdir = "/home/burak/Documents/kod/loogle/sub"

def test_simple():
    loogle2.index(cdir, index_dir, new_index=True)
    loogle2.index(cdir, index_dir)
    res = loogle2.search("scientist", index_dir)
    print (res)

def test_add_new():
    loogle2.index(cdir, index_dir, new_index=True)
    fout = open(cdir + "/new_book.txt","w")
    fout.write("doctor something something blabla scientist")
    fout.close()
    loogle2.index(cdir, index_dir)
    res = loogle2.search("doctor", index_dir)
    print (res)
    res = loogle2.search("scientist", index_dir)
    print (res)
    rsync.deleteFile(cdir + "/new_book.txt")

def test_delete():
    loogle2.index(cdir, index_dir, new_index=True)
    fout = open(cdir + "/new_book.txt","w")
    fout.write("doctor something something blabla scientist")
    fout.close()
    loogle2.index(cdir, index_dir)
    res = loogle2.search("doctor", index_dir)
    print (res)
    res = loogle2.search("scientist", index_dir)
    print (res)
    rsync.deleteFile(cdir + "/new_book.txt")
    loogle2.index(cdir, index_dir)
    res = loogle2.search("doctor", index_dir)
    print (res)


    
if __name__ == "__main__":
    #test_simple()
    #test_add_new()
    test_delete()

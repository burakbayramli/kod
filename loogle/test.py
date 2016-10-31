import sys; sys.path.append("..")
import os, shutil, loogle, unittest

idx = "%s/loogle" % os.environ['TEMP']

def prepare_dir():
    if os.path.isdir(idx): shutil.rmtree(idx)
    os.mkdir(idx)
    print idx

def create_delete_me():
    f = "./sub/delete_me.txt"
    if os.path.isfile(f): os.remove(f)
    fout = open(f,"w")
    fout.write("whassup makaka ")
    fout.close()
    return f
    
def test_simple():
    prepare_dir()

    loogle.index(crawl_dir="sub",index_dir=idx,get_first_N=10)

    res = loogle.search("scientist", index_dir=idx)
    print res
    assert 'a1' in res[0]
    
    res = loogle.search("manifesto", index_dir=idx)
    print res
    assert 'b1' in res[0]

    res = loogle.search("drug bill", index_dir=idx)
    print res
    assert 'b2' in res[0]

    res = loogle.search("amaranth", index_dir=idx)
    print res
    assert '2006_10_01_archive.html' in res[0]

    res = loogle.search("quadratic form", index_dir=idx)
    print res
    assert 'tmp.pdf' in res[0]

    res = loogle.search("interest rate cash flow", index_dir=idx)
    print res
    assert 'veryveryimpo.txt' in res[0]

    res = loogle.search("veryveryimpo", index_dir=idx)
    print res
    assert 'veryveryimpo.txt' in res[0]    

def test_inc():
    prepare_dir()
    loogle.index(crawl_dir="sub",index_dir=idx,get_first_N=2)
    loogle.index(crawl_dir="sub",index_dir=idx,get_first_N=4)
    loogle.index(crawl_dir="sub",index_dir=idx)
    res = loogle.search("interest rate cash flow", index_dir=idx)
    assert 'veryveryimpo.txt' in res[0]

def test_del():
    prepare_dir()
    f=create_delete_me()
    loogle.index(crawl_dir="sub",index_dir=idx)
    res = loogle.search("makaka", index_dir=idx)
    assert len(res) == 1
    os.remove(f)
    loogle.index(crawl_dir="sub",index_dir=idx)
    res = loogle.search("makaka", index_dir=idx)
    assert len(res) == 0

def test_update():
    prepare_dir()
    f = create_delete_me()
    loogle.index(crawl_dir="sub",index_dir=idx)
    fin = open(f,"a")
    fin.write("\n tonkatoy \n")
    fin.close()
    loogle.index(crawl_dir="sub",index_dir=idx)
    res = loogle.search("tonkatoy", index_dir=idx)
    assert 'delete_me' in res[0]    
    os.remove(f)    
    
if __name__ == "__main__":
    test_simple()
    test_inc()
    test_del()
    test_update()
    

from diskdict import DiskDict

def test1():
    dd = DiskDict('testdict')
    dd['ssdfsf'] = 34234

def test2():    
    dd = DiskDict('testdict')
    print (dd['ssdfsf'])

test2()

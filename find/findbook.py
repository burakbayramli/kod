import re
fin = open("/tmp/searchout")
for line in fin.readlines():
    res = re.findall("file:///.*?", line)
    print (res)
    exit()
    


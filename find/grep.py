# -*- coding: utf-8 -*-
# Usage:
# find . -name '*.tex' -exec python $HOME/kod/find.py '[string]'  {} \;
import sys, re
fin = open(sys.argv[2])
count = 1
for line in fin.readlines():
#    if sys.argv[1] in line:
    if re.search(sys.argv[1], line)!=None:
        print "%s:%d:%s" % (sys.argv[2], count, line)
    count += 1
fin.close()

import os, sys
sys.path.append(os.environ['HOME'] + '/Documents/kod/loogle/')
sys.path.append(os.environ['HOME'] + '/Documents/kod')
import loogle3, rsync

index_db = os.environ['HOME'] + "/Downloads/loogle.db"
#print (sys.argv[1])

search_term = ' '.join(sys.argv[1:])
res = loogle3.search(search_term, index_db)
for x in res: print ("%s:1:-" % x)

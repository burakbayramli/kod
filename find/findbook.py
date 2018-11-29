import sys; sys.path.append('/home/burak/Documents/kod/loogle/')
import sys; sys.path.append('/home/burak/Documents/kod')
import loogle3, rsync

index_db = "/home/burak/Documents/Dropbox/loogle.db"
print (sys.argv[1])

res = loogle3.search(sys.argv[1], index_db)
for x in res: print ("%s:1:-" % x)

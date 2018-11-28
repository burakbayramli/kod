import sys; sys.path.append('/home/burak/Documents/kod/loogle/')
import sys; sys.path.append('/home/burak/Documents/kod')
import loogle2, rsync

#index_dir = "/home/burak/Downloads/.loogle"
index_dir = "/tmp/idx"
print (sys.argv[1])

res = loogle2.search(sys.argv[1], index_dir)
for x in res: print ("%s:1:-" % x)

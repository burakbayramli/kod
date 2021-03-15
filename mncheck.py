import os, sys, glob

tmp = glob.glob('/home/burak/Documents/kitaplar/*')
if (len(tmp)==0):
    print ('\n============= PARTITION NOT MOUNTED =================')
    exit(-1)
print ('running ' + sys.argv[1])
os.system(sys.argv[1])

import sys, os

if sys.argv[1] == 'ziprecoll':
    os.system("zip /opt/Downloads/dotbkps/recoll.zip -r /home/burak/.recoll/")

if sys.argv[1] == 'zip':
    os.system("zip /opt/Downloads/dotbkps/kod.zip -r /home/burak/Documents/kod/.git/")
    

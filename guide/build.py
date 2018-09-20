import os, sys

if len(sys.argv) == 1 or sys.argv[1] == 'zip':
    os.system("zip -r /tmp/mindmeld-0.1.zip .")
       

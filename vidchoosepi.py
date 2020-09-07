import glob, os, random, sys
import threading, numpy as np
import datetime, random
from rsync import ls
import select, rndplay

dir = ''
if sys.argv[1] == "pi":
    dir = "/media/pi/Seagate Backup Plus Drive/shows"
    print ('pi')
else:
    dir = "/media/burak/Seagate Backup Plus Drive/shows"
    print ('acer')
    
dirs,list = ls(dir)
print ("Files", len(list))
idx = rndplay.my_random(len(list))
f = list[idx][0]
print ("show idx selected", idx, "song", f)
cmd = "vlc '%s' -f " % f
print (cmd)
os.system(cmd)


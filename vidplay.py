import glob, os, random, sys
import threading, numpy as np
import datetime, random
from rsync import ls
import select, rndplay
dir = ''

if len(sys.argv) > 1 and sys.argv[1] == "pi":
    dir = "/media/pi/Seagate Backup Plus Drive1/shows"
    print ('pi')
else:
    dir = "/media/burak/Seagate Backup Plus Drive/shows"
    print ('acer')
    
dirs,flist = ls(dir)

def fin(s,l): return np.any([x in l for x in s])

playlist = [f[0] for f in flist if fin(['.mp4','.mkv','.avi'], f[0])]

playlist = [f for f in playlist if 'Trek' not in f]

playlist = [f for f in playlist if 'Stargate' not in f]

print (len(playlist))

idx = rndplay.my_random(len(playlist))

f = playlist[idx]

print ("show idx selected", idx, "song", f)

cmd = "vlc '%s' -f " % f

print (cmd)

os.system(cmd)


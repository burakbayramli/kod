import glob, os, random, sys
import threading, numpy as np
import datetime, random
from rsync import ls
import select, util

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

print ('count', len(playlist))

playlist = [f for f in playlist if 'SG-1' not in f]
playlist = [f for f in playlist if 'DS9' not in f]
#playlist = [f for f in playlist if 'VOYAGER' not in f]
#playlist = [f for f in playlist if 'Atlantis' not in f]
#playlist = [f for f in playlist if 'The Next Generation' not in f]
#playlist = [f for f in playlist if 'Enterprise' not in f]
playlist = [f for f in playlist if 'Firefly' not in f]
#playlist = [f for f in playlist if 'Expanse' not in f]
#playlist = [f for f in playlist if 'Lost' not in f]
#playlist = [f for f in playlist if '/TLS/' not in f]
playlist = [f for f in playlist if 'BSG' not in f]
#playlist = [f for f in playlist if 'Doctor' not in f]

print ('filterd count', len(playlist))

idx = util.my_random(len(playlist))

f = playlist[idx]

print ("show idx selected", idx)

cmd = "vlc '%s' -f " % f

print (cmd)

os.system(cmd)


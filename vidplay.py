import glob, os, random, sys
import threading, numpy as np
import datetime, random
from rsync import ls
import select, util

def pick_file():
    dir = "/media/burak/Backup Plus/shows"    
    dirs,flist = ls(dir)
    def fin(s,l): return np.any([x in l for x in s])
    playlist = [f[0] for f in flist if fin(['.mp4','.mkv','.avi'], f[0])]
    print ('count', len(playlist))
    # playlist = [f for f in playlist if 'SG-1' not in f]
    # playlist = [f for f in playlist if 'DS9' not in f]
    playlist = [f for f in playlist if 'VOYAGER' not in f]
    playlist = [f for f in playlist if 'Atlantis' not in f]
    # playlist = [f for f in playlist if 'The Next Generation' not in f]
    # playlist = [f for f in playlist if 'Enterprise' not in f]
    # playlist = [f for f in playlist if 'Firefly' not in f]
    # playlist = [f for f in playlist if 'Expanse' not in f]
    # playlist = [f for f in playlist if 'Lost' not in f]
    # playlist = [f for f in playlist if '/TLS/' not in f]
    # playlist = [f for f in playlist if 'BSG' not in f]
    # playlist = [f for f in playlist if 'Doctor' not in f]
    print ('filterd count', len(playlist))
    idx = util.my_random(len(playlist))
    #idx = util.my_random2(len(playlist))
    f = playlist[idx]
    print ("show idx selected", idx)
    return f
    
def rnd_play():
    f = pick_file()
    cmd = "vlc '%s' -f " % f
    print (cmd)
    os.system(cmd)
        
def rnd_copy():
    f = pick_file()
    cmd = "cp '%s' /home/burak/Downloads/ " % f
    print (cmd)
    os.system(cmd)
    
if __name__ == "__main__": 

    rnd_play()
    #rnd_copy()

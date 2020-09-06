# Plays mp3 files found under sys.argv[1] one by one, randomly. 
# Meant to simulate a radio.
import glob, os, random, sys
import threading, numpy as np
import datetime, random
from rsync import ls
import select, rndplay

fout = open("/tmp/vidplay.out","w")

while True:
    print ("Music Dir", sys.argv[1]    )
    dirs,list = ls(sys.argv[1])
    print ("Files", len(list))
    idx = rndplay.my_random(len(list))
    print ("show idx selected", idx, "song", list[idx][0])
    fout.write(str(list[idx][0]) + "\n")
    fout.flush()
    print ('\n')
    #cmd = "/usr/bin/ffplay -nodisp '%s'" % list[idx]
    cmd = "mplayer '%s' -fs " % list[idx][0]
    print (cmd)
    os.system(cmd)
    print ("Delete? (Press d for delete)...")
    k=""
    def input():
        global k
        i = 0
        while i < 1:
            i = i + 1
            r,w,x = select.select([sys.stdin.fileno()],[],[],2)
            if len(r) != 0:
                k  =sys.stdin.readline()


    T = threading.Thread(target=input)
    T.setDaemon(1)
    T.start()
    T.join(1) # wait for [arg] seconds
    print ("\n>>>>>>>>>" + k)
    if 'd' in k:
        print ("deleting ===================> " +  list[idx])
        cmd = "rm '%s'" % list[idx]
        os.system(cmd)


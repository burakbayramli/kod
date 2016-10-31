"""
Plays random mp3 songs that are picked from the directory given as
a parameter in the command line
"""

import mp3play, glob, random, time, sys, os, subprocess
import msvcrt, threading
from rsync import ls
from rndplay import sleep, anykeyevent

exe = "c:/Users/burak/Downloads/ffmpeg/bin/ffplay.exe"
logfile = "vidplay.log"        

class Runner(threading.Thread):
    def __init__(self,file):
        threading.Thread.__init__(self)
        self.file = file
    def run(self):
        print self.file
        self.p = subprocess.Popen([exe, "-autoexit", "-fs", "%s" % file], 
                                  stdout=subprocess.PIPE, shell=True)
        self.p.wait()
    def term(self):
        print 'terminating', self.p.pid
        os.system("kill -f %d" % self.p.pid)

if __name__ == "__main__": 

    base_dir = "e:/shows"

    dirs,files = ls(base_dir)
    
    files = [x[0] for x in files if ".avi" in x[0] or "mkv" in x[0] or "mp4" in x[0]]

    fout = open("%s/%s" % (os.environ['TEMP'],logfile), "a")
    while (True):        
        rnd = random.choice(range(len(files)))
        print len(files), 'files'
        file = files[rnd]
        file = file.replace('/','\\')
        fout.write(file)
        fout.write("\n")
        fout.flush()
        t = Runner(file)
        t.start()
        while True: 
            res = sleep(1)
            if t.is_alive()==False: break
        

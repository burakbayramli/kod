# Install ffplay executables from the web site Jukebox - just point to
# any directory it will play the files underneath randomly. Use 'n'
# for next, 'SPACE' for pause, 'SPACE' for resume, left/right arrows
# to skip back/forward 3 secs.
import mp3play, glob, random, time, sys, os, subprocess
import msvcrt, threading, datetime as dt

exe = 'c:/Users/burak/Downloads/ffmpeg/bin/ffplay'
logfile = "rndplay.log"        

def getstatusoutput(cmd):
    """Return (status, output) of executing cmd in a shell."""
    mswindows = (sys.platform == "win32")
    if not mswindows:
        return commands.getstatusoutput(cmd)
    pipe = os.popen(cmd + ' 2>&1', 'r')
    text = pipe.read()
    sts = pipe.close()
    if sts is None: sts = 0
    if text[-1:] == '\n': text = text[:-1]
    return sts, text

def deleteFile(path):
    """deletes the path entirely"""
    mswindows = (sys.platform == "win32")
    if mswindows: 
        cmd = 'DEL /F /S /Q "%s"' % path
    else:
        cmd = "rm -rf " + path
    result = getstatusoutput(cmd)
    if(result[0]!=0):
        raise RuntimeError(result[1])

def anykeyevent():
    """
    Detects a key or function key pressed and returns its ascii or scancode.
    """
    if msvcrt.kbhit():
        a = ord(msvcrt.getch())
        if a == 0 or a == 224:
            b = ord(msvcrt.getch())
            x = a + (b*256)
            return x
        else:
            return a

def sleep(secs):            
    for i in range(5*secs):
        key = anykeyevent()
        if key==100:
            print 'd is pressed'
            return 'd'
        elif key==110:
            print 'n is pressed'
            return 'n'
        elif key==32:
            print 'SPACE is pressed'
            return 'SPACE'
        elif key==19424:
            print 'leftarrow is pressed'
            return 'leftarrow'
        elif key==19936:
            print 'rightarrow is pressed'
            return 'rightarrow'
        time.sleep(0.2)

class Runner(threading.Thread):
    def __init__(self,file,pos):
        threading.Thread.__init__(self)
        self.file = file
        self.pos = pos
    def run(self):
        print 'Song ', self.file
        self.p = subprocess.Popen([exe, '-ss', str(self.pos), '-nodisp','-autoexit', '%s' % file], stdout=subprocess.PIPE, shell=True)
        print 'pid', self.p.pid
        self.p.wait()
    def term(self):
        print 'terminating', self.p.pid
        os.system("kill -f %d" % self.p.pid)

if __name__ == "__main__":  
        
    if len(sys.argv) != 2: print "Usage: rndplay.py [base directory]"; exit()
    base_dir = sys.argv[1]
    files1 = glob.glob(base_dir + '/*.mp3')
    files2 = glob.glob(base_dir + '/*.flac')
    files = files1 + files2
    print len(files), 'files'
    fout = open("%s/%s" % (os.environ['TEMP'],logfile), "a")
    while (True):
        
        rnd = random.choice(range(len(files)))
        file = files[rnd]
        file = file.replace('/','\\')
        fout.write(file)
        fout.write("\n")
        fout.flush()
        n1=dt.datetime.now()
        t = Runner(file,0)
        t.start()
        elapsed = 0
        while True: 
            res = sleep(1)
            if res == 'n':
                t.term()
                break
            elif res == 'SPACE':
                n2=dt.datetime.now()
                elapsed += (n2-n1).seconds
                print 'paused at', elapsed
                t.term()
                while True: 
                    res = sleep(1)
                    if res == 'SPACE':
                        print 'resuming at', elapsed
                        n1=dt.datetime.now()
                        t = Runner(file, elapsed)
                        t.start()
                        break
            elif res == 'd':
                print 'deleting file', file
                t.term()
                deleteFile(file)
                files = glob.glob(base_dir + '/*.mp3')
                break
            elif res== 'leftarrow':
                n2=dt.datetime.now()
                elapsed += (n2-n1).seconds
                elapsed -= 5
                print 'rewinding ', elapsed
                t.term()
                n1=dt.datetime.now()
                t = Runner(file, elapsed)
                t.start()
            elif res== 'rightarrow':
                n2=dt.datetime.now()
                elapsed += (n2-n1).seconds
                elapsed += 5
                print 'rewinding ', elapsed
                t.term()
                n1=dt.datetime.now()
                t = Runner(file, elapsed)
                t.start()
                
            if t.is_alive()==False: break

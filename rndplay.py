# Plays mp3 files found under sys.argv[1] one by one, randomly. 
# Meant to simulate a radio.
import pyaudio, struct
import glob, os, random, sys
import threading, numpy as np
import datetime, random
import select

def my_random(upper):
    CHANNELS = 1; RATE = 16000; CHUNK = 2048
    RECORD_SECONDS = 0.01; FORMAT = pyaudio.paInt16
    audio = pyaudio.PyAudio()
    stream = audio.open(format=FORMAT, channels=CHANNELS,rate=RATE, input=True,
                        frames_per_buffer=CHUNK)
    data = stream.read(CHUNK)
    r1 = random.random() * 1e5
    r2 = np.random.random() * 1e5
    r3 = float(str(datetime.datetime.utcnow())[-9:].replace(".","")) 
    r4 = np.abs(np.array(struct.unpack('iiiiiiii',data[:32])).sum())
    #print r1, r2, r3, r4
    stream.stop_stream()
    stream.close()
    audio.terminate()    
    return int((r1 + r2 + r3 + r4) % upper)

if __name__ == "__main__": 
 
    fout = open("/tmp/rndplay.out","w")

    while True:
        print "Music Dir", sys.argv[1]
        os.chdir(sys.argv[1])
        list = glob.glob("*.mp3")
        print '\n'
        idx = my_random(len(list))
        print "# of songs", len(list), 
        "song idx selected", idx, 
        "song", list[idx]
        fout.write(str(list[idx]) + "\n")
        fout.flush()
        print '\n'
        #cmd = "/usr/bin/ffplay -nodisp '%s'" % list[idx]
        cmd = 'mplayer "%s" ' % list[idx]
        print cmd
        os.system(cmd)
        print "Delete? (Press d for delete)..."
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
        print "\n>>>>>>>>>" + k
        if 'd' in k:
            print "deleting ===================> " +  list[idx]
            cmd = "rm '%s'" % list[idx]
            os.system(cmd)


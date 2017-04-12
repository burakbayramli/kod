'''
Downloads 10 songs from a few known sites.
'''
import datetime, random
import pyaudio, struct
import re, requests, os
import threading, numpy as np

urls = ["http://blognoblat.com.br/radio-noblat/jazz-instrumental/"]
#urls = ["http://132.248.192.201/Mi_musica/"]

def my_random(upper):
    CHANNELS = 1; RATE = 16000; CHUNK = 2048
    RECORD_SECONDS = 0.01; FORMAT = pyaudio.paInt16
    audio = pyaudio.PyAudio()
    stream = audio.open(format=FORMAT, channels=CHANNELS,rate=RATE, input=True,
                        frames_per_buffer=CHUNK)
    data = stream.read(CHUNK)
    r3 = float(str(datetime.datetime.utcnow())[-9:].replace(".","")) % upper
    r4 = np.abs(np.array(struct.unpack('iiiiiiii',data[:32])).sum())
    stream.stop_stream()
    stream.close()
    audio.terminate()    
    return int((r3 + r4) % upper)

def get_songs(url,songs):
    os.chdir("/home/burak/Music")
    idx = random.choice(range(len(songs)))
    if ".mp3" in songs[idx]: os.system("wget '%s/%s'" % (url,songs[idx]))

def list_songs(url):        
    response = requests.get(url)
    response_body = response.content
    regex = "a href=\"(.*?mp3)\">.*?</a>"
    songs = re.findall(regex, response_body, re.DOTALL)
    return songs

def get_random_song():
    songs = []
    url = urls[my_random(len(urls))]
    while (len(songs) < 1):
        songs = list_songs(url)
        if len(songs) < 1:
            print 'no songs found, picking a subdir'
            # get dirs to pick a subdir
            response = requests.get(url)
            response_body = response.content
            regex = "td valign.*?a href=\"(.*?)/\">.*?</a>"
            dirs = re.findall(regex, response_body, re.DOTALL)
            if len(dirs) == 0: return
            print dirs
            dir = random.choice(dirs)
            print dir
            url = url + "/" + dir
        else:
            get_songs(url,songs)


if __name__ == "__main__": 
    for i in range(10): get_random_song()
    
            

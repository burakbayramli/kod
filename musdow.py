'''
Downloads 10 songs from a few known sites.
'''
import datetime, random, rndplay
import pyaudio, struct
import re, requests, os
import threading, numpy as np

urls = ["http://blognoblat.com.br/radio-noblat/jazz-instrumental/"]
#urls = ["http://www.controlaltdelight.com/Music/"]
#urls = ["http://hypem.com/download/"] #fix

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
    url = urls[rndplay.my_random(len(urls))]
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
    
            

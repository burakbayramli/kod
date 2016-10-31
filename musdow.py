'''
Downloads 10 songs from a few known sites.
'''
import re, requests, random, os
url = "http://www.sos-jukebox.net/music"
#url = "http://hcmaslov.d-real.sci-nnov.ru/public/mp3/_jazz_"
response = requests.get(url)
response_body = response.content
regex = "a href=\"(.*?mp3)\">.*?</a>"
songs = re.findall(regex, response_body, re.DOTALL)
os.chdir("c:/Users/burak/Music")
for i in range(10):
    idx = random.choice(range(len(songs)))
    if ".mp3" in songs[idx]: os.system("wget %s/%s" % (url,songs[idx]))
   

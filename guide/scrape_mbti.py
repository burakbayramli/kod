'''
Get celebrities and their MBTI data from celebritytypes.com which
seems to be most trustworthy site on "famous people MBTI" on the net.
'''
import re, urllib, urllib2
import logging
from datetime import datetime
from datetime import timedelta
from urllib import FancyURLopener

class MyOpener(FancyURLopener):
    version = 'Mozilla/5.0 (Windows; U; Windows NT 5.1; it; rv:1.8.1.11) Gecko/20071127 Firefox/2.0.0.11'        

fout = open("/tmp/myer-briggs.txt","w")
for mbti in ['estp','esfp','estj','esfj','entj','entp','enfj','enfp',
             'istp','isfp','istj','isfj','intj','intp','infj','infp']:
    opener = MyOpener()
    url = "http://www.celebritytypes.com/%s.php" % mbti 
    h = opener.open(url)
    content = h.read()
    tmp = re.findall("<p class=\".*name\">(.*?)</p>", content)
    for g in tmp:
        fout.write(mbti.upper() + ":" + g + "\n")
        fout.flush()

fout.close()

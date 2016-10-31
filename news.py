# -*- coding: utf-8 -*-
"""
Python replacement for newsbeuter, an RSS based news reader. 
"""
import feedparser, sys, codecs
import re, requests, random, os
import re, time, os

sys.stdout = codecs.getwriter('utf8')(sys.stdout)
sys.stderr = codecs.getwriter('utf8')(sys.stderr)

import base64
def encode(key, clear):
    enc = []
    for i in range(len(clear)):
        key_c = key[i % len(key)]
        enc_c = chr((ord(clear[i]) + ord(key_c)) % 256)
        enc.append(enc_c)
    return base64.urlsafe_b64encode("".join(enc))

def decode(key, enc):
    dec = []
    enc = base64.urlsafe_b64decode(enc)
    for i in range(len(enc)):
        key_c = key[i % len(key)]
        dec_c = chr((256 + ord(enc[i]) - ord(key_c)) % 256)
        dec.append(dec_c)
    return "".join(dec)

def show():

    feeds = [
        ("The Guardian","http://www.theguardian.com/world/rss",10),
        ("Diken","http://www.diken.com.tr/feed/",-1),
        ("Cumhuriyet","http://www.cumhuriyet.com.tr/rss/son_dakika.xml",10),
        ("Al-Jazeera","http://aljazeera.com.tr/rss.xml",-1),
        ("Reuters (Top News)",'http://feeds.reuters.com/reuters/topNews',-1),
        ("Reuters (World)",'http://feeds.reuters.com/reuters/worldNews',-1),
        ("Independent, The", "http://www.independent.co.uk/news/world/rss", 10),
        ("Reuters (Business)", "http://feeds.reuters.com/reuters/businessNews",-1),        
        ('Huffington Post','http://www.huffingtonpost.com/feeds/verticals/world/index.xml',-1),
        ('BBC','http://newsrss.bbc.co.uk/rss/newsonline_world_edition/front_page/rss.xml',20),
        ("Sputnik News","http://tr.sputniknews.com/export/rss2/archive/index.xml",15),
        ("The Atlantic", "http://www.theatlantic.com/feed/all/",10),
        (u"Açık Gazete","https://www.acikgazete.com/feed",-1),
        ("Bloomberg", "https://twitrss.me/twitter_user_to_rss/?user=business",15),
        ("Deusche Welle (World)", "http://rss.dw.de/rdf/rss-en-all", 15),
        ("Deusche Welle (Europe)", "http://rss.dw.de/rdf/rss-en-eu", 15),
        ("Die Welt", "http://www.welt.de/?service=Rss", 20),
        ("ARD", "http://www.ard.de/home/ard/ARD_Startseite/21920/index.xml", 20)
    ]

    #feeds = [   ]

    for name,url,lim in feeds:
        print("\n")
        print("## " + name)
        print("\n")
        d = feedparser.parse(url)
        for i,post in enumerate(d.entries):
            if lim > 0 and i==int(lim): break
            link = post.link; title = post.title
            if len(re.findall(r"Erdo.an", title, re.IGNORECASE)) > 0: continue
            if len(re.findall(r"top.u k..las", title, re.IGNORECASE)) > 0: continue
            if len(re.findall(r"Engin Ard", title, re.IGNORECASE)) > 0: continue
            if len(re.findall(r" .ld.rd.", title, re.IGNORECASE)) > 0: continue
            if len(re.findall(r"umhurba.kan", title, re.IGNORECASE)) > 0: continue
            if len(re.findall(r"tecav.z", title, re.IGNORECASE)) > 0: continue
            print("[[%s](%s)]\n" % (unicode(title), link))

    print("\n")
    print("## Marmara")
    print("\n")
    for author, link, title in marmara():
        if len(author) > 20: author = "____"
        print("[[%s - %s](%s)]\n" % (unicode(title, 'utf-8'),
                                     unicode(author, 'utf-8'),
                                     link))

            
def marmara():
    url = "http://m.marmarayerelhaber.com/yazar.asp"
    response = requests.get(url)
    response_body = response.content
    regex = "<h1 class=\"title\">.*?\">(.*?)</a></h1>.*?<p><a href=\"(.*?)\">(.*?)</a></p>"
    arts = re.findall(regex, response_body, re.DOTALL)
    return arts

show()

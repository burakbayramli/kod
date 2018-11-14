# -*- coding: utf-8 -*-
"""
Python replacement for newsbeuter, an RSS based news reader. 
"""
import feedparser, sys, codecs
import re, requests, random, os
import re, time, os

def getnews(outfile):
    feeds = [
        ("The Guardian","http://www.theguardian.com/world/rss",10),
        ("Reuters (Top News)",'http://feeds.reuters.com/reuters/topNews',-1),
        ("Reuters (World)",'http://feeds.reuters.com/reuters/worldNews',-1),
        ("Independent, The", "http://www.independent.co.uk/news/world/rss", 10),
        ("Reuters (Business)", "http://feeds.reuters.com/reuters/businessNews",-1),
        ('BBC','http://newsrss.bbc.co.uk/rss/newsonline_world_edition/front_page/rss.xml',20),
        ("The Atlantic", "http://www.theatlantic.com/feed/all/",10),
        ("Bloomberg", "https://twitrss.me/twitter_user_to_rss/?user=business",15),
        ("Deusche Welle (World)", "http://rss.dw.de/rdf/rss-en-all", 15),
        ("Deusche Welle (Europe)", "http://rss.dw.de/rdf/rss-en-eu", 15),
        ("ARD", "http://www.ard.de/home/ard/ARD_Startseite/21920/index.xml", 20)
    ]

    fout = open(outfile, "w")
    fout.write('''
    <html>
    <head>
    <link rel="stylesheet" type="text/css" href="/static/main.css" media="screen" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    </head>
    <body>
    ''')
               
    for name,url,lim in feeds:
        fout.write("<h3>" + name + "</h3>\n")
        d = feedparser.parse(url)
        for i,post in enumerate(d.entries):
            try:
                if lim > 0 and i==int(lim): break
                link = post.link; title = post.title
                if len(re.findall(r"Turkey", title, re.IGNORECASE)) > 0: continue
                if len(re.findall(r"Turkish", title, re.IGNORECASE)) > 0: continue
                if len(re.findall(r"T.rkei", title, re.IGNORECASE)) > 0: continue
                if len(re.findall(r"Erdo.an", title, re.IGNORECASE)) > 0: continue
                if len(re.findall(r"Obama", title, re.IGNORECASE)) > 0: continue
                if len(re.findall(r"Musk", title, re.IGNORECASE)) > 0: continue
                if len(re.findall(r"Dreamer", title, re.IGNORECASE)) > 0: continue
                if len(re.findall(r" DACA", title, re.IGNORECASE)) > 0: continue
                #print("[[%s](%s)]\n" % (title, link))
                fout.write("<a href='%s'>%s</a><br/><br/>\n" % (link, title))
            except Exception as e:
                #print ('error', repr(e))
                pass
    fout.close()

if __name__ == "__main__": 
    getnews("/tmp/news.html")

# -*- coding: utf-8 -*-
"""
Python replacement for newsbeuter, an RSS based news reader. 
"""
import feedparser, sys, codecs
import re, requests, random, os
import re, time, os
from bs4 import BeautifulSoup 

if os.path.isdir("/tmp"): os.environ['TMPDIR'] = "/tmp"

def strip_html(input):
    return BeautifulSoup(input, "lxml").text

def getnews(outfile):
    feeds = [
        ("Reuters (Top News)",'http://feeds.reuters.com/reuters/topNews',-1),
        ("Reuters (World)",'http://feeds.reuters.com/reuters/worldNews',-1),
        ("Reuters (Business)", "http://feeds.reuters.com/reuters/businessNews",-1),
        ('BBC','http://newsrss.bbc.co.uk/rss/newsonline_world_edition/front_page/rss.xml',20),
        ("Independent, The", "http://www.independent.co.uk/news/world/rss", 10),
        ("Bloomberg", "https://twitrss.me/twitter_user_to_rss/?user=business",15),
        ("The Atlantic", "http://www.theatlantic.com/feed/all/",10),
        ("Deusche Welle (World)", "http://rss.dw.de/rdf/rss-en-all", 15),
        ("The Guardian","http://www.theguardian.com/world/rss",10),
        ("Deusche Welle (Europe)", "http://rss.dw.de/rdf/rss-en-eu", 15)
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
                summary = strip_html(post.summary)
                summary = re.sub('\shttp.*?pic\.twitter.*?\<','<',summary)
                summary = re.sub('\shttp.*?pic\.twitter.*?\s',' ',summary)
                summary = re.sub('Shan Wang at swang@theatlantic.com','',summary)
                title = re.sub('\shttp.*?pic\.twitter.*?\<','<',title)
                title = re.sub('\shttp.*?pic\.twitter.*?\s',' ',title)
                title = re.sub('\shttp.*?pic\.twitter.*?[$]','\n',title)

                if len(re.findall(r"Turkey", title, re.IGNORECASE)) > 0: continue
                if len(re.findall(r"Turkish", title, re.IGNORECASE)) > 0: continue
                if len(re.findall(r"T.rkei", title, re.IGNORECASE)) > 0: continue
                if len(re.findall(r"battery", title, re.IGNORECASE)) > 0: continue
                if len(re.findall(r"electric", title, re.IGNORECASE)) > 0: continue
                if len(re.findall(r"Erdo.an", title)) > 0: continue
                if len(re.findall(r"Ocasio", title)) > 0: continue
                if len(re.findall(r"Elon", title)) > 0: continue
                if len(re.findall(r"Musk", title)) > 0: continue
                if len(re.findall(r"Tesla", title)) > 0: continue                
                fout.write("<a href='%s'>%s</a><br/><br/>\n" % (link, title))
                fout.write("%s<br/><br/>\n" % (summary))
            except Exception as e:
                pass
    fout.close()

if __name__ == "__main__":
    print (os.environ['TMPDIR'])
    dir = os.environ['TMPDIR'] + "/news.html"
    print (dir)
    getnews(dir)

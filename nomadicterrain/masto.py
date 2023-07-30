import feedparser, sys, codecs, socket
import re, requests, random, os, news
from bs4 import BeautifulSoup 
import time, os, pandas as pd

accts = "static/following_accounts.csv"

def getrss():
    socket.setdefaulttimeout(5)
    feeds = [
        ("August","https://mastodon.online/@davidaugust.rss",20)
    ]

    content = ""
    content += '''
    <html>
    <head>
    <link rel="stylesheet" type="text/css" href="/static/main.css" media="screen" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    </head>
    <body>
    '''

    df = pd.read_csv(accts)
    lim = 10
    ids = list(range(10))
    for i,row in df.iterrows():
        if random.choice(ids)!=0: continue
        addr = row['Account address']
        name = addr[0:addr.find("@")]
        host = addr[addr.find("@")+1:]
        url = "https://"+ host + "/@" + name + ".rss"
        print (url)
        content += "<h3>" + addr + "</h3>\n"
        try:
            d = feedparser.parse(url)
            for i,post in enumerate(d.entries):
                if lim > 0 and i==int(lim): break
                link = post.link
                dt = post.updated
                summary = news.strip_html(post.summary)
                skip = False
                for w in news.skip_words:
                    if len(re.findall(w, summary, re.IGNORECASE)) > 0:
                        skip = True
                if skip: continue
                                
                content += "%s<br/><br/>\n" % summary
                if 'media_content' in post:
                    for link in post['media_content']:
                        content += "<img width='340' src='%s'/><br/>\n" % link['url']
                content += "%s<hr/><br/>\n" % dt
        except Exception as e:
            print (repr(e))
            continue

    return content

if __name__ == "__main__": 
    res = getrss()
    fout = open("/tmp/masto.html","w")
    fout.write(res)
    fout.close()

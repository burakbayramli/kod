from bs4 import BeautifulSoup 
import feedparser, sys, codecs
import re, requests, random, os
import re, time, os

def strip_html(input):
    return BeautifulSoup(input, "lxml").text

skip_words = ["Turk", "battery","Webb", "electric","Blinken","Biden","Thunberg",
              "lithium", "AOC", "der Leyen", "Erdo.an","Elon", "Musk","Tesla",
              "batteries", "SpaceX", "Mars","black hole", "artificial intelligence",
              " AI ", "poll", "Zelensky", "black hole", " EV ", "Webb", "A\.I\.", 
              "telescope", "Yellen"]



def getnews():
    feeds = [
        ("H2 Central","https://hydrogen-central.com/feed/",20),
        ("H2 Fuel News","https://www.hydrogenfuelnews.com/feed/",10),
        ("Al Jazeera","https://www.aljazeera.com/xml/rss/all.xml",10),
        ("The Mandarin AU","https://www.themandarin.com.au/feed/",15),
        ("ABC Australia","https://www.abc.net.au/feeds/9443166/podcast.xml",10),
        ("Independent, The", "http://www.independent.co.uk/news/world/rss", 10),
        ("The Guardian","http://www.theguardian.com/world/rss",10),
        ('Al Monitor','https://www.al-monitor.com/rss',20),
        ("Arab News","https://www.arabnews.com/cat/3/rss.xml",10),
        ("Jane's Defense", "https://www.janes.com/feeds/news", 10),        
        ('CNBC','https://www.cnbc.com/id/100727362/device/rss/rss.html',10),        
        ('WSJ','https://feeds.a.dj.com/rss/RSSWorldNews.xml',10),
        ("NYT", "https://rss.nytimes.com/services/xml/rss/nyt/World.xml",15),
        ("WION","https://www.wionews.com/feeds/world/rss.xml",20),
        ("Hindustan Times World","https://www.hindustantimes.com/feeds/rss/world-news/rssfeed.xml",10),
        ("Politico.eu","https://www.politico.eu/feed/",5),
        ("First Post","https://www.firstpost.com/rss/world.xml",10),
        ("France 24","https://www.france24.com/en/rss",10),        
        ("Politico","https://www.politico.com/rss/politicopicks.xml",10),
        ("TDB","https://feeds.thedailybeast.com/summary/rss/articles",10),
        ("H2 View","https://www.h2-view.com/rss/feed/e15e1d6422c35afec038fbd559797494/",10),
        ("The Atlantic", "https://www.theatlantic.com/feed/all",8)
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
    
    for name,url,lim in feeds:
        print (name)
        content += "<h3>" + name + "</h3>\n"
        try:
            d = feedparser.parse(url)
            for i,post in enumerate(d.entries):
                if lim > 0 and i==int(lim): break
                link = post.link; title = post.title
                summary = strip_html(post.summary)
                summary = re.sub('\shttp.*?pic\.twitter.*?\<','<',summary)
                summary = re.sub('\shttp.*?pic\.twitter.*?\s',' ',summary)
                summary = re.sub('Shan Wang at swang@theatlantic.com','',summary)
                summary = re.sub('appeared first on The Intercept','',summary)
                summary = summary.replace(" appeared first on The Mandarin","")
                title = re.sub('\shttp.*?pic\.twitter.*?\<','<',title)
                title = re.sub('\shttp.*?pic\.twitter.*?\s',' ',title)
                title = re.sub('\shttp.*?pic\.twitter.*?[$]','\n',title)
                skip = False
                for w in skip_words:
                    if len(re.findall(w, title, re.IGNORECASE)) > 0:
                        skip = True
                    if len(re.findall(w, summary, re.IGNORECASE)) > 0:
                        skip = True
                if skip: continue
                                
                content += "<a href='%s'>%s</a><br/><br/>\n" % (link, title)
                content += "%s<br/><br/>\n" % (summary)
        except Exception as e:
            print (repr(e))
            continue
        
    return content

if __name__ == "__main__": 
    res = getnews()
    fout = open("/tmp/news.html","w")
    fout.write(res)
    fout.close()
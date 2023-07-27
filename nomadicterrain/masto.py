from bs4 import BeautifulSoup 
import feedparser, sys, codecs
import re, requests, random, os
import re, time, os

def strip_html(input):
    return BeautifulSoup(input, "lxml").text

skip_words = ["Turk", "Türkiye", "battery","Webb", "electric","Blinken","Biden","Ocasio",
              "lithium", "AOC", "der Leyen", "Erdo.an","Elon", "Musk","Tesla",
              "batteries", "SpaceX", "Mars","black hole", "artificial intelligence",
              " AI ", "AI ", " AI", "poll", "Zelensky", "black hole", " EV ", "Webb",
              "A\.I\.", "telescope", "Yellen", "Francis","Thunberg","tweet","charging",
              "tweets","twitter","ChatGPT", "EVs", "electrification", "charger","AI's",
              "Jon Stewart", "quantum", "power grid", "ronaldo", "Wagner", "Trump",
              "LGBTQ+", "Cathie Wood"]



def getnews():
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
    
    for name,url,lim in feeds:
        print (name)
        content += "<h3>" + name + "</h3>\n"
        try:
            d = feedparser.parse(url)
            for i,post in enumerate(d.entries):
                if lim > 0 and i==int(lim): break
                link = post.link
                summary = strip_html(post.summary)
                summary = re.sub('\shttp.*?pic\.twitter.*?\<','<',summary)
                summary = re.sub('\shttp.*?pic\.twitter.*?\s',' ',summary)
                summary = re.sub('Shan Wang at swang@theatlantic.com','',summary)
                summary = re.sub('appeared first on The Intercept','',summary)
                summary = summary.replace(" appeared first on The Mandarin","")
                skip = False
                for w in skip_words:
                    if len(re.findall(w, summary, re.IGNORECASE)) > 0:
                        skip = True
                if skip: continue
                                
                content += "%s<br/><br/>\n" % (summary)
        except Exception as e:
            print (repr(e))
            continue
        
    return content

if __name__ == "__main__": 
    res = getnews()
    fout = open("/tmp/masto.html","w")
    fout.write(res)
    fout.close()

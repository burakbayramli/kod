from bs4 import BeautifulSoup
import requests, re, sys

headers_Get = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64; rv:49.0) Gecko/20100101 Firefox/49.0',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
        'Accept-Language': 'en-US,en;q=0.5',
        'Accept-Encoding': 'gzip, deflate',
        'DNT': '1',
        'Connection': 'keep-alive',
        'Upgrade-Insecure-Requests': '1'
    }

def visible(element):
   if element.parent.name in ['style', 'script', '[document]', 'head', 'title']:
       return False
   elif re.match('<!--.*-->', str(element)):
       return False
   return True

def google(q):
    s = requests.Session()
    q = '+'.join(q.split())
    url = 'https://www.google.com/search?q=' + q + '&ie=utf-8&oe=utf-8'
    print (url)
    r = s.get(url, headers=headers_Get)

    soup = BeautifulSoup(r.text,features="lxml")
    texts = soup.findAll(text=True)
    visible_texts = filter(visible, texts)
    content = ""
    for x in visible_texts:
        content += x
    
    return content

if __name__ == "__main__": 
 
    res = google(sys.argv[1])
    print (res)


# -*- coding: utf-8 -*-
from urllib.request import urlopen
from bs4 import BeautifulSoup
from urllib.request import urlretrieve
import codecs, os, re

base = 'http://sayilarvekuramlar.blogspot.com'
urls = ['/2018/11/tensorflowjs-javascript-ile-tensorflow.html',
        '/2018/11/ses-komutlar-tanma-ve-aktarml-ogrenme.html',
        '/2018/11/onceden-egitilmis-modeller-pre-trained.html']

def get_article(url, local):
    fname = url[url.rfind('/')+1:]    
    subdir = local + url[0:url.rfind('/')]
    if not os.path.isdir(subdir):
        os.makedirs(subdir)
    md_file = subdir + "/" + fname.replace(".html",".md")
    if os.path.isfile(md_file):
        print ('Already downloaded', url)
        return
    fout = codecs.open(md_file, "w", encoding='utf-8')
    html = urlopen(base + url)
    bsObj = BeautifulSoup(html.read(),"lxml");
    title = bsObj.h3.get_text().strip()
    fout.write("# " + title + "\n")
    content = bsObj.get_text()
    
    imgs = bsObj.find_all("img")
    imgs = [x.get('src') for x in imgs if "bp.blogspot.com" in x.get('src')]
    tmp_img = []
    for img in imgs:
        print (img)
        imgname = img[img.rfind('/')+1:]
        urlretrieve(img, subdir + "/" + imgname)
        tmp_img.append(imgname)
    
    active = False
    for i,line in enumerate(content.split("\n")):
        if i==480: active = True
        if u'Gönderen' in line: active = False
        if active:
            fout.write(line)
            fout.write("\n")

    imgs = bsObj.find_all("img")
    for img in tmp_img:
        fout.write("![](%s)\n" % img)
        
    fout.close()

def articles():
    d = {}

    fin = open("/home/burak/Downloads/blog-11-25-2018.xml")
    content = fin.read()
    res = re.findall("sayilarvekuramlar.blogspot.com/(.*?.html)",
                     content,
                     re.DOTALL)

    count = 0
    for i,x in enumerate(res):
        if "feeds" in x: continue
        if "/html" in x: continue
        if len(x) < 150:
            count += 1
            d[x] = "1"
            print (x)

    print (len(d))
    #print (d)
    
        
if __name__ == "__main__":
    local = "/tmp/sk"
    res = articles()
    for x in res:
        print (x)
        try:
            get_article(x, local)
        except Exception as e:
            print ("cannot get article", x, repr(e))    
# -*- coding: utf-8 -*-
from urllib.request import urlopen
from bs4 import BeautifulSoup
from urllib.request import urlretrieve
import codecs, os

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

    
if __name__ == "__main__":    
    local = "/tmp/sk"
    get_article(urls[2], local)
    

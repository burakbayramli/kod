# downloads edible / not-edible information for plant from wikipedia
import urllib.request, re
from bs4 import BeautifulSoup
import pandas as pd
outfile = "/home/burak/Downloads/out_cuis.csv"
def process():    
    fin = open(outfile)
    d = {}
    for x in fin.readlines(): d[x.split("|")[0]] = "1"
    fin.close()
    fout = open(outfile,"a")
    df = pd.read_csv('/home/burak/Downloads/plants.csv',encoding='utf-8')
    for p in list(df['Scientific Name']):
        plant = p.replace(" ","_")
        print (plant)
        edible = ""
        if p in d:
            print ('already downloaded...skipping')
            continue
        try:
            with urllib.request.urlopen('https://en.wikipedia.org/wiki/' + plant) as response:
                html = response.read()
                res = re.findall('id="Cuisine.*?<p>(.*?)</p>', str(html))
                if len(res) > 0:
                    t = res[0]
                    soup = BeautifulSoup(t, "html.parser")
                    for a in soup("a"): a.replace_with(a.text)
                    for a in soup("i"): a.replace_with(a.text)
                    for a in soup("sup"): a.replace_with(a.text)
                    print(str(soup))
                    edible = str(soup)
        except urllib.error.HTTPError as e:
            print (repr(e))
            
        fout.write("%s|%s\n" % (p, edible) )
        fout.flush()        
    fout.close()

def test1():
    ps = ['Achillea millefolium','Cinna latifolia','Chenopodium album']
    for p in ps:
        print (p)
        plant = p.replace(" ","_")
        with urllib.request.urlopen('https://en.wikipedia.org/wiki/' + plant) as response:
           html = response.read()
           res = re.findall('<h3><span class="mw-headline" id="Food".*?<p>(.*?)</p>', str(html))
           if len(res) > 0:
               t = res[0]
               soup = BeautifulSoup(t, "html.parser")
               for a in soup("a"): a.replace_with(a.text)
               for a in soup("sup"): a.replace_with(a.text)
               print(str(soup))

def test2():
    ps = ['Achillea millefolium','Cinna latifolia','Colocasia']
    for p in ps:
        print (p)
        plant = p.replace(" ","_")
        with urllib.request.urlopen('https://en.wikipedia.org/wiki/' + plant) as response:
           html = response.read()
           res = re.findall('id="Culinary.*?<p>(.*?)</p>', str(html))
           if len(res) > 0:
               t = res[0]
               soup = BeautifulSoup(t, "html.parser")
               for a in soup("a"): a.replace_with(a.text)
               for a in soup("sup"): a.replace_with(a.text)
               print(str(soup))

def test3():
    ps = ['Capsicum']
    for p in ps:
        print (p)
        plant = p.replace(" ","_")
        with urllib.request.urlopen('https://en.wikipedia.org/wiki/' + plant) as response:
           html = response.read()
           res = re.findall('id="Cuisine.*?<p>(.*?)</p>', str(html))
           if len(res) > 0:
               t = res[0]
               soup = BeautifulSoup(t, "html.parser")
               for a in soup("a"): a.replace_with(a.text)
               for a in soup("i"): a.replace_with(a.text)
               for a in soup("sup"): a.replace_with(a.text)
               print(str(soup))

process()
#test3()

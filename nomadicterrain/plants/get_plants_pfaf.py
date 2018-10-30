# downloads edible / not-edible information for plant from wikipedia
import urllib.request, re
from bs4 import BeautifulSoup
import pandas as pd
outfile = "/home/burak/Downloads/out_pfaf.csv"
base_url = "https://pfaf.org/user/Plant.aspx?LatinName="    

def process():    
    fin = open(outfile)
    d = {}
    for x in fin.readlines(): d[x.split("|")[0]] = "1"
    fin.close()
    fout = open(outfile,"a")
    df = pd.read_csv('/home/burak/Downloads/plants.csv',encoding='utf-8')
    for p in list(df['Scientific Name']):
        if p in d:
            print ('already downloaded...skipping')
            continue
        edible = ""
        try:
            plant = p.replace(" ","+")
            url = base_url + plant
            print (url)
            req = urllib.request.Request(
                url, 
                data=None, 
                headers={
                    'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.47 Safari/537.36'
                }
            )
            with urllib.request.urlopen(req) as response:
               html = response.read()
               res = re.findall('<h2>Edible Uses</h2>.*?<p>(.*?)</p>', str(html))
               if len(res) > 0:
                   t = res[0]
                   soup = BeautifulSoup(t, "html.parser")
                   for a in soup("span"): a.replace_with(a.text)
                   aa = str(soup)
                   aa = aa.replace(r"\r\n", r"\n")
                   aa = aa.replace(r"\n", r"")
                   if aa.isspace(): aa = ""
                   aa = aa.strip()
                   print(aa)
                   edible = aa
        except urllib.error.HTTPError as e:
            print (repr(e))
        fout.write("%s|%s\n" % (p, edible) )
        fout.flush()        
    fout.close()

def test3():
    base_url = "https://pfaf.org/user/Plant.aspx?LatinName="    
    #ps = ['Capsicum','Solanum dulcamara']
    ps = ['Capsicum']
    for p in ps:
        print (p)
        plant = p.replace(" ","+")
        url = base_url + plant
        print (url)
        req = urllib.request.Request(
            url, 
            data=None, 
            headers={
                'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.47 Safari/537.36'
            }
        )
        with urllib.request.urlopen(req) as response:
           html = response.read()
           res = re.findall('<h2>Edible Uses</h2>.*?<p>(.*?)</p>', str(html))
           if len(res) > 0:
               t = res[0]
               soup = BeautifulSoup(t, "html.parser")
               for a in soup("span"): a.replace_with(a.text)
               aa = str(soup)
               aa = aa.replace(r"\r\n", r"\n")
               aa = aa.replace(r"\n", r"")
               print(aa)

process()
#test3()

import re
from bs4 import BeautifulSoup as bs
from unidecode import unidecode

"""
Processes the KML file downloaded from https://www.kampyerleri.org/
"""
def kamp_yerleri():
    content = open("/tmp/trkamp.kml").read()
    fout = open("/tmp/kampyerleri.csv","w")
    res = re.findall(r'<name>(.*?)</name>.*?<description>(.*?)</description>.*?<coordinates>(.*?)</coordinates>', content, re.DOTALL)
    fout.write("%s|%s|%s\n" % ("name","description","location"))
    for i,x in enumerate(res):
        if i==0: continue
        soup=bs(x[1],'lxml')
        txt = soup.get_text()
        txt = txt.replace("<![CDATA[","")
        txt = txt.replace("]]>","")
        txt = txt.replace("\n","").strip()
        txt = txt.replace("|", " ")
        name = x[0].strip()
        name = name.replace("<![CDATA[","")
        name = name.replace("]]>","")
        fout.write("%s;%s;%s\n" % (name,x[2].strip().replace(",0",""),unidecode(txt)))
        fout.flush()

    fout.close()

if __name__ == "__main__": 
    kamp_yerleri()

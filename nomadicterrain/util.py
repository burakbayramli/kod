import re, folium, pandas as pd
from bs4 import BeautifulSoup as bs
from unidecode import unidecode

"""
Processes the KML file downloaded from https://www.kampyerleri.org/
"""
def kamp_yerleri():
    content = open("/tmp/trkamp.kml").read()
    fout = open("/tmp/kampyerleri.csv","w")
    res = re.findall(r'<name>(.*?)</name>.*?<description>(.*?)</description>.*?<coordinates>(.*?)</coordinates>', content, re.DOTALL)
    fout.write("%s;%s;%s\n" % ("name","location","description"))
    for i,x in enumerate(res):
        if i==0: continue
        soup=bs(x[1],'lxml')
        txt = soup.get_text()
        txt = txt.replace("<![CDATA[","")
        txt = txt.replace("]]>","")
        txt = txt.replace("\n","").strip()
        txt = txt.replace("|", " ")
        txt = txt.replace(";", " ")
        txt = unidecode(txt)
        name = x[0].strip()
        name = name.replace("<![CDATA[","")
        name = name.replace("]]>","")
        loc = x[2].strip().replace(",0","")
        fout.write("%s;%s;%s\n" % (name,loc,txt))
        fout.flush()

    fout.close()

def camp_folium():
    df = pd.read_csv('data/kampyerleri.csv',sep=';')
    m = folium.Map(location=[39, 33], zoom_start=7, tiles="Stamen Terrain")
    for index, row in df.iterrows():
        lat,lon = (float(x) for x in row['location'].split(","))
        folium.Marker(
            [lon,lat], popup=row['description'],
        ).add_to(m)
        
    df = pd.read_csv('data/trkamp2.csv',sep=';')
    for index, row in df.iterrows():
        lat,lon = (float(x) for x in row['location'].split(","))
        folium.Marker(
            [lat,lon], popup=row['name'],
        ).add_to(m)
    m.save('/tmp/trcamp-out.html')
    
def camp_folium_yayla():
    df = pd.read_csv('data/kampyerleri.csv',sep=';')
    m = folium.Map(location=[39, 33], zoom_start=7, tiles="Stamen Terrain")
    for index, row in df.iterrows():
        if 'Yayla' not in row['name']: continue
        lat,lon = (float(x) for x in row['location'].split(","))
        folium.Marker(
            [lon,lat], popup=row['name'] + " " + row['description'],
        ).add_to(m)
        
    m.save('/tmp/yayla-out.html')
    
if __name__ == "__main__": 
    #kamp_yerleri()
    #camp_folium()
    camp_folium_yayla()

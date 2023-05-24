import re, folium, pandas as pd
from bs4 import BeautifulSoup as bs
from unidecode import unidecode
import numpy as np

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

"""
    p1 = np.array([[1, 2]])
    p2 = np.array([[4, 6], [2, 1], [7, 8]])
    my_cdist(p1,p2)
    print (cdist(p1,p2))
"""
def cdist(p1,p2):    
    distances = np.linalg.norm(p1 - p2, axis=1)
    print (distances)
        
if __name__ == "__main__": 
    #kamp_yerleri()
    #camp_folium()
    #camp_folium_yayla()

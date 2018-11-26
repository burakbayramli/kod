import itertools, time
import pandas as pd, time
import numpy as np
import matplotlib.pyplot as plt
from math import sin, cos, sqrt, atan2, radians
from io import BytesIO
from PIL import Image
import urllib.request, os.path


def get_map(lat, lon, region, zoom):
    api = open("/home/burak/Documents/Dropbox/google_static_map_api.txt").read()
    url = "http://maps.googleapis.com/maps/api/staticmap?center=" + \
    	  "%f,%f&size=800x800&maptype=terrain&zoom=%d&key=%s" % (lat,lon,zoom,api)
    print (url)
    lats = str(lat).replace(".","_")
    lons = str(lon).replace(".","_")
    fout = "%s/%s_map_%s_%s.png" % (region,region,lats,lons)
    if os.path.isfile(fout):
        print ("Already downloaded...")
        return False
    buffer = BytesIO(urllib.request.urlopen(url).read())
    image = Image.open(buffer)
    image.save(fout)
    return True
    
def get_maps(c1,c2,px,py,region,zoom=11):
    """
    c1: one corner of the region box
    c2: the opposite corner of the region box

    get_maps will always pick the smallest / largest of each
    coord and create a box to sweep over. 
    """
    a= np.linspace(min(c1[0],c2[0]), max(c1[0],c2[0]), px)
    b= np.linspace(min(c1[1],c2[1]), max(c1[1],c2[1]), py)
    aa,bb = np.meshgrid(a,b)
    for x,y in zip(aa.flatten(),bb.flatten()):
        if get_map(x,y,region,zoom) == False: continue


if __name__ == "__main__":
    # harita hangi kordinatlar arasinda olmali
    #c1 = (51.450320,2.963884); c2 = (39.460801, 29.786351)
    #c1 = (42.085563, 25.213239); c2 = (36.367053, 45.453831)
    c1 = (41.978983, 25.139504); c2 = (33.359696, 45.361736)
    get_maps(c1, c2, 80, 80, region="turkey2")

    # 42.085563, 25.213239
    # 36.367053, 45.453831
    

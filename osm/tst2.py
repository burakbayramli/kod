# latitude -90,90
# longitude -180,180
from diskdict import DiskDict
import csv, numpy as np, re
from pygeodesy.sphericalNvector import LatLon

fr=(41.01437162347757,29.164254494113184)
to=(41.0497882628352,29.2460494538482)

frClosestDist = 100000; frClosestNode = 'x'
toClosestDist = 100000; toClosestNode = 'x'
frInt = (str(int(fr[0])), str(int(fr[1])))
toInt = (str(int(to[0])), str(int(to[1])))
with open('nodes.csv') as csvfile:
    rd = csv.reader(csvfile,delimiter=',')
    headers = {k: v for v, k in enumerate(next(rd))}
    for i,row in enumerate(rd):        
        if i % 10000 == 0: print (i)
        id,lat,lon = row[headers['id']],row[headers['lat']],row[headers['lon']]
        intlat = int(lat.split(".")[0])
        intlon = int(lon.split(".")[0])
        if intlat == frInt[0] or intlon == frInt[1]:
            p1 = LatLon(fr[0], fr[1])
            p2 = LatLon(lat,lon)
            d = p1.distanceTo(p2) 
            if d < frClosestDist:
                frClosestNode = row[headers['id']]
                frClosestDist = d
        if intlat or intlon == toInt[1]:
            p1 = LatLon(to[0], to[1])
            p2 = LatLon(lat,lon)
            d = p1.distanceTo(p2) 
            if d < toClosestDist:
                toClosestNode = row[headers['id']]
                toClosestDist = d
                    
print (frClosestDist,frClosestNode)
print (toClosestDist,toClosestNode)


            

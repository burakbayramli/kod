import numpy as np, matplotlib.pyplot as plt
import csv, numpy as np, re, pickle
from scipy.spatial.distance import cdist

from pygeodesy.sphericalNvector import LatLon
res1 = LatLon(36.52259447316748, 27.612981046240638)
res2 = LatLon(41.05628025861666, 42.58542464923075)

lowlat = np.min([res1.lat,res2.lat])
lowlon = np.min([res1.lon,res2.lon])
hilat = np.max([res1.lat,res2.lat])
hilon = np.max([res1.lon,res2.lon])

x = np.linspace(lowlon,hilon,7)
y = np.linspace(lowlat,hilat,4)

xx,yy = np.meshgrid(x,y)

mids = []
for x,y in zip(xx.flatten(), yy.flatten()):
    mids.append([x,y])

mids = np.array(mids)

pickle.dump(mids, open('tr_centers.pkl', 'wb'))        
exit()

fout = open ("nodes2.csv","w")
fout.write("%s,%s,%s,%d,%d\n" % ('id','lat','lon','c1','c2'))

with open('nodes.csv') as csvfile:
    rd = csv.reader(csvfile,delimiter=',')
    headers = {k: v for v, k in enumerate(next(rd))}
    for i,row in enumerate(rd):        
        id,lat,lon = row[headers['id']],row[headers['lat']],row[headers['lon']]
        #if i > 1000: break
        ds = cdist(mids,np.array([[lon,lat]]))
        res = list(np.argsort(ds,axis=0).T[0][:2])
        fout.write("%s,%s,%s,%d,%d\n" % (id,lat[:8],lon[:8],res[0],res[1]))
        fout.flush()
        if i % 100000 == 0:
            print ('i',i)
            

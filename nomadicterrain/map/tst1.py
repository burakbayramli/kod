import os, glob, re, zipfile
import pandas as pd, pickle
import numpy as np, sqlite3, json
from PIL import Image
import geopy.distance, route
import pandas as pd, io

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

lat1,lon1 = (42.431028999999995, 18.694765)
#lat1,lon1 = (42.0001, 18.002)

boxlat1,boxlon1 = route.goto_from_coord((lat1,lon1), 10.0, 45)
boxlat2,boxlon2 = route.goto_from_coord((lat1,lon1), 10.0, 215)

boxlatlow = np.min([boxlat1,boxlat2])
boxlonlow = np.min([boxlon1,boxlon2])
boxlathigh = np.max([boxlat1,boxlat2])
boxlonhigh = np.max([boxlon1,boxlon2])

D = 20
x = np.linspace(boxlonlow,boxlonhigh,D)
y = np.linspace(boxlatlow,boxlathigh,D)
xi = np.unique([int(xx) for xx in x])
yi = np.unique([int(yy) for yy in y])
print (xi)
print (yi)

#xx,yy = np.meshgrid(x,y)
#print (xx)
#print (yy)

exit()

conn = sqlite3.connect(params['elevdb'])
c = conn.cursor()
    
sql = "SELECT latlow,lathigh,lonlow,lonhigh,W from RBF1 where latint=? and lonint=?"
res = c.execute(sql,(int(lat1),int(lon1)))
windows = []
Ws = []
for latlow,lathigh,lonlow,lonhigh,W in res:
    windows.append([latlow,lathigh,lonlow,lonhigh])
    Ws.append(W)
    
windows = pd.DataFrame(windows)
windows.columns = ['latlow','lathigh','lonlow','lonhigh']
Ws = pd.DataFrame(Ws)
print (windows)

res = windows.apply(lambda x: \
                    lat1>x.latlow and \
                    lon1>x.lonlow and \
                    lat1<x.lathigh and \
                    lon1<x.lonhigh, \
                    axis=1)
print (res)








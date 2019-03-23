from mpl_toolkits.mplot3d import Axes3D
import matplotlib.pyplot as plt
import os, glob, re, zipfile
import pandas as pd, pickle
import numpy as np, sqlite3, json
from PIL import Image
import geopy.distance, route
import pandas as pd, io

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

lat1,lon1 = (42.431028999999995, 18.694765)


boxlat1,boxlon1 = route.goto_from_coord((lat1,lon1), 10.0, 45)
boxlat2,boxlon2 = route.goto_from_coord((lat1,lon1), 10.0, 215)

boxlatlow = np.min([boxlat1,boxlat2])
print (boxlatlow)
boxlonlow = np.min([boxlon1,boxlon2])
boxlathigh = np.max([boxlat1,boxlat2])
boxlonhigh = np.max([boxlon1,boxlon2])

D = 20
x = np.linspace(boxlonlow,boxlonhigh,D)
y = np.linspace(boxlatlow,boxlathigh,D)

#print (xx)
#print (yy)

conn = sqlite3.connect(params['elevdb'])
c = conn.cursor()

xi = np.unique([int(xx) for xx in x])
yi = np.unique([int(yy) for yy in y])
print (xi)
print (yi)
windows = []
Ws = []
for latint in yi:
    for lonint in xi:
        print (latint,lonint)
        sql = "SELECT latlow,lathigh,lonlow,lonhigh,W from RBF1 where latint=? and lonint=?"
        res = c.execute(sql,(int(latint),int(lonint)))
        for latlow,lathigh,lonlow,lonhigh,W in res:
            windows.append([latlow,lathigh,lonlow,lonhigh])
            Ws.append(W)
    
windows = pd.DataFrame(windows)
#print (windows)
windows.columns = ['latlow','lathigh','lonlow','lonhigh']
Ws = np.array(Ws)

def isin(lat,lon):
    res = windows.apply(lambda x: \
                        lat>x.latlow  and \
                        lon>x.lonlow  and \
                        lat<x.lathigh and \
                        lon<x.lonhigh, \
                        axis=1)
    W = Ws[res]
    rbfi = pickle.loads(W[0])
    return rbfi

W = isin(lat1,lon1)
xx,yy = np.meshgrid(x,y)
zz = np.zeros(xx.shape)
for i in range(xx.shape[0]):
    for j in range(xx.shape[1]):
        #print (yy[i,j],xx[i,j])
        rbfi = isin(yy[i,j],xx[i,j])        
        znew = rbfi(xx[i,j],yy[i,j])
        if znew > 0.0: zz[i,j] = znew


plon,plat = np.round(float(lon1),3),np.round(float(lat1),3)

plt.figure()
plt.plot(plon,plat,'rd')
cs=plt.contour(xx,yy,zz,10)
plt.clabel(cs,inline=1,fontsize=9)
plt.savefig('/data/data/com.termux/files/home/Downloads/out.png')

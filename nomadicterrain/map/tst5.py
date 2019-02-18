# -*- coding: utf-8 -*-

import pandas as pd
import numpy as np, plot_map, json, os, re
import matplotlib.pyplot as plt, csv
import geopy.distance, math, route, polyline
from scipy.interpolate import interp1d
import math, pyproj

P = pyproj.Proj(proj='utm', zone=33, ellps='WGS84', preserve_units=True)
G = pyproj.Geod(ellps='WGS84')

def LatLon_To_XY(Lat,Lon):
    return P(Lat,Lon)

def XY_To_LatLon(x,y):
    return P(x,y,inverse=True)

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

def dist(x1,y1,x2,y2,px,py):
    a = np.array([[x1,y1]]).T
    b = np.array([[x2,y2]]).T
    x = np.array([[px,py]]).T
    tp = (np.dot(x.T, b) - np.dot(a.T, b)) / np.dot(b.T, b)
    tp = tp[0][0]
    tmp = x - (a + tp*b)
    d = np.sqrt(np.dot(tmp.T,tmp)[0][0])
    return d, (a + tp*b)

p = [35.266684, 33.357093]

roi = [[35.323294, 33.308268],
       [35.289657, 33.307907],
       [35.323202, 33.373341]]

print (route.dist_to_roi_outer(roi, p))

exit()

l = map(lambda x: LatLon_To_XY(x[0],x[1]),roi)
roi2 = [[x[0],x[1]] for x in l]

df = pd.DataFrame(roi2)

r = np.roll(roi2,-1,axis=0)
df[2] = r[:,0]
df[3] = r[:,1]
df.columns = ['x1','y1','x2','y2']

p2 = LatLon_To_XY(p[0],p[1])

df['d'] = df.apply(lambda r: dist(r['x1'],r['y1'],r['x2'],r['y2'],p2[0],p2[1])[0],axis=1)
df['i'] = df.apply(lambda r: dist(r['x1'],r['y1'],r['x2'],r['y2'],p2[0],p2[1])[1],axis=1)

df.to_csv('out.csv')

print (df['d'].idxmin())

c = df.ix[df['d'].idxmin(),'i'].flatten()
c = XY_To_LatLon(c[0],c[1])

roi.insert(0,[p[0],p[1]])
roi.insert(0,[c[0],c[1]])

zfile,scale = params['mapzip']['turkey3']
plot_map.plot(roi,'/data/data/com.termux/files/home/Downloads/out.png',zfile=zfile,scale=scale)

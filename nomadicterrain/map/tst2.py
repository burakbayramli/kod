# -*- coding: utf-8 -*-

import pandas as pd
import numpy as np, plot_map, json, os, re
import matplotlib.pyplot as plt, csv
import geopy.distance, math, route, polyline
from scipy.interpolate import interp1d
import math

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

x1,y1=2.,2.
x2,y2=5.,5.
px,py=4.,1.

print (dist(x1,y1, x2,y2, px,py))

roi = [[35.323294, 33.308268],
       [35.289657, 33.307907],
       [35.323202, 33.373341]
       ]

df = pd.DataFrame(roi)
r = np.roll(roi,-1,axis=0)
df[2] = r[:,0]
df[3] = r[:,1]
df.columns = ['y1','x1','y2','x2']

p = [35.266684, 33.357093]

df['d'] = df.apply(lambda r: dist(r['x1'],r['y1'],r['x2'],r['y2'],p[1],p[0])[0],axis=1)
df['i'] = df.apply(lambda r: dist(r['x1'],r['y1'],r['x2'],r['y2'],p[1],p[0])[1],axis=1)

print (df['d'])

print (df['d'].idxmin())

c = df.ix[df['d'].idxmin(),'i'].flatten()
print (c[1],c[0])

roi.insert(0,[p[0],p[1]])
roi.insert(0,[c[1],c[0]])

zfile,scale = params['mapzip']['turkey3']
plot_map.plot(roi,'/data/data/com.termux/files/home/Downloads/out.png',zfile=zfile,scale=scale)


# https://jswhit.github.io/pyproj/


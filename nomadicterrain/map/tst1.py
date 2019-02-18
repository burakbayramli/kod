
# -*- coding: utf-8 -*-
import numpy as np, plot_map, json, os, re
import matplotlib.pyplot as plt, csv
import geopy.distance, math, route, polyline
from scipy.interpolate import interp1d
import math

def dist1(x1,y1, x2,y2, xp,yp):
    px = x2-x1
    py = y2-y1
    something = px*px + py*py
    u =  ((xp - x1) * px + (yp - y1) * py) / float(something)
    if u > 1:
        u = 1
    elif u < 0:
        u = 0        
    x = x1 + u * px
    y = y1 + u * py    
    dx = x - xp
    dy = y - yp
    d = math.sqrt(dx*dx + dy*dy)
    
    return d, (x,y)

def dist2(x1,y1,x2,y2,px,py):
    a = np.array([[x1,y1]]).T
    b = np.array([[x2,y2]]).T
    x = np.array([[px,py]]).T
    tp = (np.dot(x.T, b) - np.dot(a.T, b)) / np.dot(b.T, b)
    tp = tp[0][0]
    tmp = x - (a + tp*b)
    print ('tp',tp)
    d = np.sqrt(np.dot(tmp.T,tmp)[0][0])
    return d, (a + tp*b)

x1,y1=2.,2.
x2,y2=5.,5.
#px,py=4.,1.
px,py=2.,6.

#print (dist(x1,y1, x2,y2, px,py))

#d,c = dist2(x1,y1, x2,y2, px,py)
d,c = dist2(x1,y1, x2,y2, px,py)
print (d,c)

plt.xlim(0,10)
plt.ylim(0,10)
plt.plot(x1,y1,'rd')
plt.plot(x2,y2,'rd')
plt.plot(px,py,'rd')
plt.plot(c[0],c[1],'bd')

plt.savefig('/data/data/com.termux/files/home/Downloads/out.png')







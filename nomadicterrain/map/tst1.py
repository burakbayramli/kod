# -*- coding: utf-8 -*-
import numpy as np, plot_map, json, os, re
import matplotlib.pyplot as plt, csv
import geopy.distance, math, route, polyline
from scipy.interpolate import interp1d
import math

def dist(x1,y1, x2,y2, xp,yp):
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
    dist = math.sqrt(dx*dx + dy*dy)

    print (u)    
    print (dx,dy)    
    
    return dist

x1,y1=2,2
x2,y2=5,5
px,py=4,0

plt.plot(x1,y1,'rd')
plt.plot(x2,y2,'rd')
plt.plot(px,py,'rd')
plt.savefig('/data/data/com.termux/files/home/Downloads/out.png')

print (dist(x1,y1, x2,y2, px,py))


params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())
                   
#x = [[35.323294, 33.308268],[35.289657, 33.307907],[35.323202, 33.373341]]
#x = np.array(x)


#zfile,scale = params['mapzip']['turkey3']
#plot_map.plot(res,'/data/data/com.termux/files/home/Downloads/out.png',zfile=zfile,scale=scale)






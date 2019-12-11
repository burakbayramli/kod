

```python
from mpl_toolkits.mplot3d import Axes3D
from matplotlib.colors import LightSource
from matplotlib import cm
import os, glob, re, zipfile
import pandas as pd, pickle
import numpy as np, sqlite3, json
import matplotlib.pyplot as plt
from PIL import Image
import geopy.distance, route

def plot(points,outfile,scale,pixel=False,bp=True):
    plt.figure()
    center_res = points[0]
    imgcoord = []
    found_file = "/home/burak/Downloads/6144.tile"
    mapcenter = np.array([40.970041,29.070311])
    print (mapcenter)    
    im = Image.open(found_file)
    nim = np.array(im)
    c = nim.shape[0] / 2, nim.shape[0] / 2
    plt.axis('off')
    fig=plt.imshow(im)
    fig.axes.get_xaxis().set_visible(False)
    fig.axes.get_yaxis().set_visible(False)
    plt.imshow(im)
    print (c)
    for i,[lat,lon] in enumerate(points):
        dx,dy=((lon-mapcenter[1])*scale[0],(lat-mapcenter[0])*scale[1])
        print (dx,dy)
        xx = c[0]+dx
        yy = c[1]+dy
        xx,yy=(393.79057777673006,561.5153343658894)
        if xx > nim.shape[0] or yy > nim.shape[1] or xx<0 or yy<0: continue
        if i==0:
            if bp: plt.plot(xx,yy,'rx')
            else: plt.plot(xx,yy,'r,')
        else:
            if pixel:
                plt.plot(xx,yy,'r,')
            else:
                plt.plot(xx,yy,'r.')
    plt.savefig(outfile, bbox_inches='tight', pad_inches = 0, dpi = 300)

lat1,lon1=40.970041,29.070311
lat3,lon3=40.968254,29.080640
plot(points=[[lat3,lon3]], outfile="/tmp/out.png", scale=[-30000,20000])

```

```text
[40.970041 29.070311]
(400.0, 400.0)
-309.869999999961 -35.7400000000041
```










```python
import json, os
params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())
import plot_map
#pts = [[48.306219, 7.452300]] # near mountain
#pts = [[51.2130605,4.4174822]] # antwerp
#pts = [[40.987659,29.036428],[40.992186,29.039228]] # tr
#pts = [[36.551907, 32.193444]] # alanya park
#pts = [[52.510811, 13.370794]] # pots
#pts = [[36.557532, 32.064841]] # alanya
pts = [[40.267429, 28.946028]] 
#zfile,scale = params['mapzip']['terrain']
#zfile,scale = params['mapzip']['istanbul']
#zfile,scale = params['mapzip']['world2']
zfile = '/home/burak/Downloads/campdata/turkey1.zip'
scale = [1600,-2000]
print (scale)
plot_map.plot(pts,'out.png',zfile=zfile,scale=scale)
```

```text
[1600, -2000]
turkey1/turkey1_map_40_233304620253165_28_97916830379747.png
[40.233304620253165 28.97916830379747]
```


```python
import get_map
#get_map.get_map(48.788836,2.898861, ".", zoom=11)
get_map.get_map(36.525643, 32.090421, ".", zoom=12)
```

```text
http://maps.googleapis.com/maps/api/staticmap?center=36.525643,32.090421&size=800x800&scale=2&maptype=terrain&zoom=12&key=AIzaSyCxQopw-CIBAdyUrhYk18LC_qurTjUWWlE

Out[1]: True
```











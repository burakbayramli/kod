

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
import subprocess, os, json

def plot(points,outfile,pixel=False,bp=True):
    plt.figure()
    res = load_map(pts)
    pixels = res['pixels']
    found_file = res['file']
    im = Image.open(found_file)
    nim = np.array(im)
    plt.axis('off')
    fig=plt.imshow(im)
    fig.axes.get_xaxis().set_visible(False)
    fig.axes.get_yaxis().set_visible(False)
    plt.imshow(im)
    for i,[xx,yy] in enumerate(pixels):
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

def load_map(pts):
    spts = str([str(pt[0]) + ";" + str(pt[1]) for pt in pts])
    spts = spts.replace('[','').replace(']','')
    spts = spts.replace("'","").replace(" ","")
    cmd = ['/bin/sh',os.environ['HOME']+'/Documents/kod/nomadicterrain/map/staticmap/run.sh', spts,'/tmp','/home/burak/Downloads/turkey.map','14']
    result = subprocess.run(cmd, stdout=subprocess.PIPE)
    res = json.loads(result.stdout.decode('utf-8'))
    return res

lat1,lon1=40.970041,29.070311
lat2,lon2=40.971041,29.071311
lat3,lon3=40.968254,29.080640
pts = [[lat1,lon1],[lat2,lon2],[lat3,lon3]]
plot(points=pts, outfile="/tmp/out.png")

```

```text
[[40.970041, 29.070311], [40.971041, 29.071311], [40.968254, 29.08064]]
[[17.723164443857968, 475.3467543730512], [54.132053333334625, 427.126049364917], [393.79057777673006, 561.5153343658894]]
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











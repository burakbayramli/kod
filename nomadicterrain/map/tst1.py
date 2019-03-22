import os, glob, re, zipfile
import pandas as pd, pickle
import numpy as np, sqlite3, json
from PIL import Image
import geopy.distance
import pandas as pd, io

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

lat1,lon1 = (42.431028999999995, 18.694765)

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

res = windows.apply(lambda x: lat1>x.latlow and lon1>x.lonlow and lat1<x.lathigh and lon1 < x.lonhigh, axis=1)
print (res)








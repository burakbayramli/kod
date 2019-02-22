import numpy.linalg as lin, zipfile, io
import geopy.distance, sqlite3, csv
from urllib.request import urlopen
import numpy as np, polyline, json
import os, pickle, math
import numpy as np, pandas as pd

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

lat2,lon2 = (36.872073, 30.653719)

zfile = params['hydrolines']
zip_file    = zipfile.ZipFile(zfile)
items_file  = zip_file.open('hydrolines.csv')
items_file  = io.TextIOWrapper(items_file)
rd = csv.reader(items_file,delimiter="|")
headers = {k: v for v, k in enumerate(next(rd))}
for i,row in enumerate(rd):
    try:
        pts = []
        r1 = eval(row[1])
        pts.append(r1[0])
        pts.append(r1[-1])    
        if len(r1) > 2:
            mid = int(len(r1)/2.)
            pts.append(r1[mid])
        print (pts)
        dists = [geopy.distance.vincenty((lat,lon),(lat2,lon2)).km for (lon,lat) in pts]
        d = np.min(np.array(dists))
        if d < 10.0: print (len(r1))
    except Exception as e:
        print ('-------------')
        print (repr(e))
        continue

#with zipfile.ZipFile(zfile) as z:
#    with z.open("hydrolines.csv") as f:
#        for i,line in enumerate(f):
#            print (line)
#            if i==10: break
    

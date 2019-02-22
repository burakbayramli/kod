import numpy.linalg as lin, zipfile, io
import geopy.distance, sqlite3, csv
from urllib.request import urlopen
import numpy as np, polyline, json
import os, pickle, math, re
import numpy as np, pandas as pd

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

zfile = params['hydrolines']

lat2,lon2 = (36.872073, 30.653719)

with zipfile.ZipFile(zfile) as z:
    with z.open("hydrolines.csv") as f:
        for i,line in enumerate(f):
            if i==0: continue
            line = str(line)
            line = line.replace("\\n","")
            line = line.replace("'","")
            line = line.replace('"','')
            line = line.split("|")
            toks = line[1].split(",")
            toks = np.array([float(t) for t in toks]).reshape(2,-1)
            print (toks)
            if i==10: break

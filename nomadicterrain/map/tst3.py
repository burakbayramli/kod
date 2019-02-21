from scipy.spatial.distance import cdist
import numpy.linalg as lin
import geopy.distance, sqlite3
from urllib.request import urlopen
import numpy as np, polyline, json
import os, pickle, math
import numpy as np, pandas as pd
from pqdict import pqdict

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())
        
conn = sqlite3.connect(params['elevdb'])
c = conn.cursor()

c.execute('''DROP TABLE RBF1; ''')
c.execute('''CREATE TABLE RBF1 (latint INT, lonint INT, latlow REAL, lathigh REAL, lonlow REAL, lonhigh REAL, W BLOB); ''')
conn.commit()

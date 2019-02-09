from matplotlib.colors import LightSource
from scipy.spatial.distance import cdist
import numpy as np, sqlite3, json, os
from mpl_toolkits.mplot3d import Axes3D
import matplotlib.pyplot as plt
from matplotlib import cm
import matplotlib.pyplot as plt
import numpy.linalg as lin
import pandas as pd, pickle
import geopy.distance, route

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

conn = sqlite3.connect(params['elevdb'])
c = conn.cursor()

lat1,lon1=(36.549177, 31.981221)

sql = "SELECT W, gamma from RBF1 where ?>=latlow and ?<lathigh and ?>=lonlow and ?<lonhigh "
res = c.execute(sql,(lat1,lat1,lon1,lon1))
res = list(res)
if (len(res)!=1): raise Exception()
W,gamma = res[0]
print (gamma)
df = pickle.loads(W)
#print (df)

lat2,lon2 = (36.07653,32.836227) # anamur

def get_grid(lat,lon,step):
    pts = []
    pts.append(route.goto_from_coord((lat,lon), step, 0))
    pts.append(route.goto_from_coord((lat,lon), step, 45))
    pts.append(route.goto_from_coord((lat,lon), step, 90))
    pts.append(route.goto_from_coord((lat,lon), step, 135))
    pts.append(route.goto_from_coord((lat,lon), step, 180))
    pts.append(route.goto_from_coord((lat,lon), step, 225))
    pts.append(route.goto_from_coord((lat,lon), step, 270))
    pts.append(route.goto_from_coord((lat,lon), step, 315))
    return pts

neighs = get_grid(lat1,lon1,0.1)

xr=np.array(df[0])
xr=xr.reshape(len(xr),1)
yr=np.array(df[1])
yr=yr.reshape(len(xr),1)
X = np.hstack((xr,yr))

xnew = np.array([[lon1,lat1]])
print (np.multiply(df.w.T,np.exp(-gamma*lin.norm(X-xnew,axis=1))).sum())
print ('-')
for pt in neighs:
    xnew = np.array([[pt[1],pt[0]]])
    print (np.multiply(df.w.T,np.exp(-gamma*lin.norm(X-xnew,axis=1))).sum())





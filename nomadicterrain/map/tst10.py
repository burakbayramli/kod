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
from pqdict import pqdict

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

conn = sqlite3.connect(params['elevdb'])
c = conn.cursor()

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

def get_elev_single(lat,lon):
    sql = "SELECT W, gamma from RBF1 where ?>=latlow and ?<lathigh and ?>=lonlow and ?<lonhigh "
    r = c.execute(sql,(lat,lat,lon,lon))
    r = list(r)
    if (len(r)!=1): raise Exception()
    W,gamma = r[0]
    df = pickle.loads(W)
    xr=np.array(df[0])
    xr=xr.reshape(len(xr),1)
    yr=np.array(df[1])
    yr=yr.reshape(len(xr),1)
    X = np.hstack((xr,yr))
    xnew = np.array([[lon,lat]])
    return np.multiply(df.w.T,np.exp(-gamma*lin.norm(X-xnew,axis=1))).sum()

def get_neighs(lat,lon):
    sql = "SELECT W, gamma from RBF1 where ?>=latlow and ?<lathigh and ?>=lonlow and ?<lonhigh "
    r = c.execute(sql,(lat,lat,lon,lon))
    r = list(r)
    if (len(r)!=1): raise Exception()
    W,gamma = r[0]
    df = pickle.loads(W)
    xr=np.array(df[0])
    xr=xr.reshape(len(xr),1)
    yr=np.array(df[1])
    yr=yr.reshape(len(xr),1)
    X = np.hstack((xr,yr))    
    neighs = get_grid(lat,lon,0.1)
    res = []
    for pt in neighs:
        xnew = np.array([[pt[1],pt[0]]])
        res.append((pt[0],pt[1],np.multiply(df.w.T,np.exp(-gamma*lin.norm(X-xnew,axis=1))).sum()))
    return res

def dijkstra(s,e):    
    D = {}
    P = {}
    Q = pqdict()
    Q[s] = 0

    while len(Q)>0:
        (v,vv) = Q.popitem()
        if geopy.distance.vincenty(v,e).km < 0.100: break
        D[v] = vv
        neighs = get_neighs(v[0],v[1])
        velev = get_elev_single(v[0],v[1])
        for (wlat,wlon,welev) in neighs:
            w = (wlat,wlon)
            if welev < 0.0: continue # skip negative candidates
            vwLength = D[v] + np.abs(velev - welev)
            if w in D:
                if vwLength < D[v]:
                    raise ValueError("error")
            elif w not in Q or vwLength < Q[w]:
                Q[w] = vwLength
                P[w] = v
            
    path = []
    while 1:
       path.append(e)
       if e == s: break
       e = P[e]
    path.reverse()
    return path

lat1,lon1=(36.549177, 31.981221)
lat2,lon2 = (36.07653,32.836227) # anamur
r = dijkstra((lat1,lon1),(lat2,lon2))
print (r)


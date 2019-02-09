from matplotlib.colors import LightSource
from scipy.spatial.distance import cdist
import numpy as np, sqlite3, json, os
from mpl_toolkits.mplot3d import Axes3D
import matplotlib.pyplot as plt
from matplotlib import cm
import matplotlib.pyplot as plt
import numpy.linalg as lin, random
import pandas as pd, pickle
import geopy.distance, route
from pqdict import pqdict

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

conn = sqlite3.connect(params['elevdb'])
c = conn.cursor()

def dijkstra(C,s,e):    
    D = {}
    P = {}
    Q = pqdict()
    Q[s] = 0

    while len(Q)>0:
        (v,vv) = Q.popitem()
        D[v] = vv
        neighs = get_neighbor_idx(v[0],v[1],C.shape)
        for w in neighs:
            if C[w[0],w[1]] < 0.0: continue # skip negative candidates
            vwLength = D[v] + np.abs(C[v[0],v[1]] - C[w[0],w[1]])
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
        
def get_grid(lat1,lon1,lat2,lon2,npts):
   def pointiterator(fra,til,steps):    
       val = fra
       if til < fra:
           til += 360.0
       stepsize = (til - fra)/steps
       while val < til + stepsize:
           if (val > 180.0):
               yield val - 360.0
           else:
               yield val
           val += stepsize

   xiter = pointiterator(np.min([lat1,lat2]),np.max([lat1,lat2]),npts)
   yiter = pointiterator(np.min([lon1,lon2]),np.max([lon1,lon2]),npts)

   xx=np.fromiter(xiter,dtype=np.float)
   yy=np.fromiter(yiter,dtype=np.float)
   print (xx.shape)
   print (yy.shape)
   xo, yo = np.meshgrid(xx,yy,indexing='xy')
   print ('xo',xo.shape)
   print ('yo',yo.shape)
   return xo,yo

def get_elev_single(lat,lon):
    sql = "SELECT W, gamma from RBF1 where ?>=latlow and ?<lathigh and ?>=lonlow and ?<lonhigh "
    r = c.execute(sql,(lat,lat,lon,lon))
    r = list(r)
    return -10.0
    W,gamma = r[0]
    df = pickle.loads(W)
    xr=np.array(df[0])
    xr=xr.reshape(len(xr),1)
    yr=np.array(df[1])
    yr=yr.reshape(len(xr),1)
    X = np.hstack((xr,yr))
    xnew = np.array([[lon,lat]])
    return np.multiply(df.w.T,np.exp(-gamma*lin.norm(X-xnew,axis=1))).sum()

def get_neighbor_idx(x,y,dims):
    res = []
    for i in ([0,-1,1]):
        for j in ([0,-1,1]):
            if i==0 and j==0: continue
            if x+i<(dims[0]) and x+i>-1 and y+j<(dims[1]) and y+j>-1:
                res.append((x+i,y+j))
    return res

def get_elev_data(lat1,lon1,lat2,lon2,npts):
    xo,yo = get_grid(lat1,lon1,lat2,lon2,npts=npts)
    start_idx = None
    end_idx = None

    for eps in [0.003, 0.01, 0.1, 1.0]:
        for i in range(xo.shape[0]):
            for j in range(xo.shape[1]):
                if np.abs(xo[i,j]-lat1)<eps and np.abs(yo[i,j]-lon1)<eps:
                    start_idx = (i,j)
                if np.abs(xo[i,j]-lat2)<eps and np.abs(yo[i,j]-lon2)<eps:
                    end_idx = (i,j)
        if start_idx!=None and end_idx != None: break
         
    print ('s',start_idx)
    print ('e',end_idx)

    elev_mat = np.zeros(xo.shape)   
    for i in range(xo.shape[0]):
        for j in range(xo.shape[1]):
            get_elev_single(xo[i,j],yo[i,j])

    
    return elev_mat, start_idx, end_idx, xo, yo 


lat1,lon1=(36.549177, 31.981221)
lat2,lon2 = (36.07653,32.836227) # anamur

elev_mat, start_idx, end_idx, xo, yo = get_elev_data(lat1,lon1,
                                                     lat2,lon2,
                                                     npts=200)
print ('data retrieved')
p = dijkstra(elev_mat, start_idx, end_idx)

print (p)


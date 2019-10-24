from mpl_toolkits.mplot3d import Axes3D
from scipy.spatial.distance import cdist
from matplotlib.colors import LightSource
from matplotlib import cm
import matplotlib.pyplot as plt
from scipy.interpolate import Rbf
import numpy as np, plot_map, json, os
import geopy.distance, math, route, autograd
from datetime import timedelta
import datetime, sqlite3, pickle, re
import autograd.numpy as anp

DIV = 2.0
OFFSET = 1000.0
SROWS = 40000
mu = 2.0
LIM = 2.0
alpha = 0.05

params = json.loads(open(os.environ['HOME'] + "/Downloads/campdata/nomterr.conf").read())

def dist_matrix(X, Y):
    sx = anp.sum(X**2, 1)
    sy = anp.sum(Y**2, 1)
    D2 =  sx[:, anp.newaxis] - 2.0*X.dot(Y.T) + sy[anp.newaxis, :] 
    D2[D2 < 0] = 0
    D = anp.sqrt(D2)
    return D
    
def gaussian(r,eps):
    return anp.exp(-(r/eps)**2.0)

def get_elev(pts,connmod):
    res = get_pts_rbf(pts, connmod)
    elevs = f_elev(pts, res)
    return elevs

def f_elev2(pts, xis, nodes, epsilons):    
    print (pts.shape)
    pts_elevs = {}
   
def get_rbf_for_latlon_ints(latlons, connmod):
    cm = connmod.cursor()
    xis = {}
    nodes = {}
    epsilons = {}
    for (latint, lonint) in latlons:
        print (latint, lonint)
        for lati in range(10):
            for lonj in range(10):
                sql = "SELECT W from ELEVRBF where latint=? and lonint=? " + \
                      "and lati=? and lonj=? " 
                r = cm.execute(sql,(int(latint),int(lonint),int(lati),int(lonj)))
                r = list(r)
                rbfi = r[0]
                rbfi = pickle.loads(rbfi[0])
                xis[(latint,lonint,lati,lonj)] = anp.array([x for x in rbfi.xi])
                nodes[(latint,lonint,lati,lonj)] = anp.array([x for x in rbfi.nodes])
                epsilons[(latint,lonint,lati,lonj)] = rbfi.epsilon
                      
    return xis, nodes, epsilons

def test_rbf_get():
    connmod = sqlite3.connect(params['elevdbmod'])
    ls = [[41,30],[41,31]]
    xis, nodes, epsilons = get_rbf_for_latlon_ints(ls,connmod)

def test_dist():
    connmod = sqlite3.connect(params['elevdbmod'])
    xis, nodes, epsilons = get_rbf_for_latlon_ints([[41,31]],connmod)
    lat1,lon1 = 41.084967,31.126588
    f_elev2(anp.array([[lat1,lon1]]), xis, nodes, epsilons)
    
test_dist()
#test_obj()
#test_rbf_get()


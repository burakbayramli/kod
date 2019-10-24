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

def get_pts_rbf(pts, connmod):
    cm = connmod.cursor()
    keyList = {}
    if 'ArrayBox' in str(type(pts)):
        pts = pts._value
    #print ('pts',pts)
    for pt in pts:
        lat,lon=pt[0],pt[1]
        latint,lonint = int(lat),int(lon)
        lati = str(lat).split(".")[1][0]
        lonj = str(lon).split(".")[1][0]
        keyList[latint,lati,lonint,lonj] = "-"
    res = {}
    for (lat,lati,lon,lonj) in keyList:
        sql = "SELECT W from ELEVRBF where latint=? and lonint=? " + \
              "and lati=? and lonj=? " 
        r = cm.execute(sql,(int(lat),int(lon),int(lati),int(lonj)))
        r = list(r)
        if len(r)==1:
            rbfi = r[0]
            rbfi = pickle.loads(rbfi[0])
            xi = anp.array([x for x in rbfi.xi])
            nodes = anp.array([x for x in rbfi.nodes])
            res[(lat,lati,lon,lonj)] = (xi, nodes, rbfi.epsilon)
    return res

def get_elev(pts,connmod):
    res = get_pts_rbf(pts, connmod)
    elevs = f_elev(pts, res)
    return elevs
    
def f_elev(pts, rbf_dict):
    pts_elevs = {}
    pts_rbfs = {}
    for k in rbf_dict.keys(): pts_rbfs[k] = []
    if 'ArrayBox' in str(type(pts)):
        pts = pts._value
    #print ('pts',pts)
    for (lat,lon) in pts:
        latm = str(int(lat))
        lonm = str(int(lon))
        lati = str(lat).split(".")[1][0]
        lonj = str(lon).split(".")[1][0]
        kk = (int(latm),lati,int(lonm),lonj)
        pts_rbfs[kk].append([lat,lon])
        
    for k in pts_rbfs.keys():
        pts = anp.array(pts_rbfs[k])
        (xi, nodes, epsilon)  = rbf_dict[k]
        pts_dist = dist_matrix(pts, xi.T)
        elev = anp.dot(gaussian(pts_dist, epsilon), nodes.T)
        elev = anp.reshape(elev,(len(elev),1))
        rec = anp.hstack((pts, elev))
        for pt,z in zip(pts,elev):
            pts_elevs[str(pt)] = z

    return pts_elevs

def trapz(y, dx):
    vals = anp.array([_ if anp.isnan(_)==False else OFFSET for _ in y[1:-1]])
    #vals = anp.array([_ for _ in y[1:-1]])
    tmp = anp.sum(vals*2.0)    
    return (y[0]+tmp+y[-1])*(dx/2.0)

def path_integral(a0,b0,ex,ey):
    connmod = sqlite3.connect(params['elevdbmod'])
    t = anp.linspace(0,1.0,100)
    def obj(xarg):
        a1,a2,a3,b1,b2,b3=xarg[0],xarg[1],xarg[2],xarg[3],xarg[4],xarg[5]
        a4 = ex - a0 - (a1+a2+a3)
        b4 = ey - b0 - (b1+b2+b3)
        sq = anp.sqrt(b1 + 2*b2*t + 3*b3*t**2 - 112.0*t**3 + (a1 + 2*a2*t + 3*a3*t**2 - 65.2*t**3)**2)
        x = a0 + a1*t + a2*t**2 + a3*t**3 + a4*t**4 
        y = b0 + b1*t + b2*t**2 + b3*t**3 + b4*t**4
        pts = anp.vstack((y,x))
        print (pts.shape)
        res = get_pts_rbf(pts.T, connmod)
        z = f_elev(pts.T,res)
        z = anp.array([xx[0] for xx in z.values()])
        res = z * sq
        T = trapz(res, 1.0/len(t))        
        cons = mu * (anp.log(LIM+a1) + anp.log(LIM-a1) + \
                     anp.log(LIM+a2) + anp.log(LIM-a2) + \
                     anp.log(LIM+a3) + anp.log(LIM-a3) + \
                     anp.log(LIM+b1) + anp.log(LIM-b1) + \
                     anp.log(LIM+b2) + anp.log(LIM-b2) + \
                     anp.log(LIM+b3) + anp.log(LIM-b3))
        T = T - cons
        if ('ArrayBox' not in str(type(T))):
            return float(T)
        return T._value

    anp.random.seed(0)
    a1,a2,a3 = anp.random.randn()/DIV, anp.random.randn()/DIV, anp.random.randn()/DIV
    b1,b2,b3 = anp.random.randn()/DIV, anp.random.randn()/DIV, anp.random.randn()/DIV
    #a1,a2,a3,b1,b2,b3=0.2,0.4,0.6,0.6,0.4,0.2
    newx = anp.array([a1,a2,a3,b1,b2,b3])
    print ('obj',obj(newx))

    j = autograd.jacobian(obj)
    J = j(newx)
    print (J)

def test_obj():
    lat1,lon1 = 41.084967,31.126588
    lat2,lon2 = 40.749752,31.610694
    path_integral(lon2,lat2,lon1,lat1)
    
#test_single_rbf_block()    
#main_test()
#pts_elev_test()
#test_topo()
test_obj()

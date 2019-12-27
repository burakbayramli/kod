from scipy.optimize import minimize, Bounds, SR1, BFGS
import json, os, sqlite3
import pandas as pd
import numpy as np
import itertools, pickle
import matplotlib.pyplot as plt

MAX = 10000.

epsilon = np.sqrt(np.finfo(float).eps)

def _approx_fprime_helper(xk, f):
    f0 = f(xk)    
    grad = np.zeros((len(xk),), float)
    ei = np.zeros((len(xk),), float)
    for k in range(len(xk)):
        ei[k] = 1.0
        d = epsilon * ei
        df = (f(xk + d) - f0) / d[k]
        if not np.isscalar(df):
            try:
                df = df.item()
            except (ValueError, AttributeError):
                raise ValueError("The user-provided "
                                 "objective function must "
                                 "return a scalar value.")
        grad[k] = df
        ei[k] = 0.0
    return grad

def trapz(y, dx):
    vals = y[1:-1]
    vals = vals[vals>0.0]
    return (y[0]+np.sum(vals*2.0)+y[-1])*(dx/2.0)

def f_elev(pts, xis, nodes, epsilons):    
    pts_elevs = {}
    for (lat,lon) in pts:
        if np.isnan(lat) or np.isnan(lon): continue
        latm = int(lat)
        lonm = int(lon)            
        lati = int(str(lat).split(".")[1][0])
        lonj = int(str(lon).split(".")[1][0])
        node = nodes[(latm,lonm,lati,lonj)]
        xi = xis[(latm,lonm,lati,lonj)]
        epsilon = epsilons[(latm,lonm,lati,lonj)]
        pts_dist = dist_matrix(np.array([[lat,lon]]), xi.T)        
        elev = np.dot(gaussian(pts_dist, epsilon), node.T)
        elev = np.reshape(elev,(len(elev),1))        
        pts_elevs[(lat,lon)] = elev[0][0]
    return pts_elevs

def dist_matrix(X, Y):
    sx = np.sum(np.power(X,2), 1)
    sy = np.sum(np.power(Y,2), 1)
    D2 =  sx[:, np.newaxis] - np.dot(2.0*X,Y.T) + sy[np.newaxis, :]
    tmp = [x for x in D2[0] if x>0.0 ]
    D2 = np.array([tmp])    
    D = np.sqrt(D2)
    return D

def gaussian(r,eps):
    return np.exp(-np.power((r/eps),2.0))

def get_rbf_for_latlon_ints(latlons, connmod):
    cm = connmod.cursor()
    xis = {}
    nodes = {}
    epsilons = {}
    for (latint, lonint) in latlons:
        for lati in range(10):
            for lonj in range(10):
                sql = "SELECT W from ELEVRBF where latint=? and lonint=? " + \
                      "and lati=? and lonj=? " 
                r = cm.execute(sql,(int(latint),int(lonint),int(lati),int(lonj)))
                r = list(r)
                if len(r)>0:
                    rbfi = r[0]
                    rbfi = pickle.loads(rbfi[0])
                    xis[(latint,lonint,lati,lonj)] = np.array([x for x in rbfi['xi']])
                    nodes[(latint,lonint,lati,lonj)] = np.array([x for x in rbfi['nodes']])
                    epsilons[(latint,lonint,lati,lonj)] = np.float(rbfi['epsilon'])
                else:
                    xis[(latint,lonint,lati,lonj)] = np.ones((2,10))*MAX
                    nodes[(latint,lonint,lati,lonj)] = np.ones((1,10))*MAX
                    epsilons[(latint,lonint,lati,lonj)] = MAX
                      
    return xis, nodes, epsilons


def find_path(a0,b0,ex,ey,xis,nodes,epsilons):
    print ('----')
    print (a0,b0,ex,ey)
    t = np.linspace(0,1.0,200)
    
    def calc_int(pars):
        a1,a2,a3,b1,b2,b3=pars
        a4 = ex - a0 - (a1+a2+a3)
        b4 = ey - b0 - (b1+b2+b3)
        def gfunc(t):        
            t = t[0]
            x = a0 + a1*t + a2*t**2 + a3*t**3 + a4*t**4 
            y = b0 + b1*t + b2*t**2 + b3*t**3 + b4*t**4
            pts = np.vstack((y,x))
            res = f_elev(pts.T, xis, nodes, epsilons)
            res = list(res.values())[0]
            return res
        ts = np.linspace(0.0,1.0,100)
        dzs = np.array([_approx_fprime_helper([t],gfunc)[0] for t in ts])
        tmp = np.sqrt(1.0+(dzs**2.0))
        Iv = trapz(tmp, 1/100.)
        tmp = np.array([b1 + 2*b2*t + 3*b3*t**2 - 112.0*t**3 + (a1 + 2*a2*t + 3*a3*t**2 - 65.2*t**3)**2 for t in ts])
        tmp = tmp[tmp>0.0]
        tmp = np.sqrt(tmp)
        Ih = trapz(tmp, 1/100.)
        res = Iv*5 + Ih*1
        return res 
            
    LIM = 5.0
    a1,a2,a3 = 0,0,0
    b1,b2,b3 = 0,0,0
    x0 = a1,a2,a3,b1,b2,b3

    opts = {'maxiter': 300, 'verbose': 0}
    res = minimize (fun=calc_int,
                    x0=x0,
                    method='trust-constr',
                    hess = BFGS (),
                    bounds=Bounds([-LIM, -LIM, -LIM, -LIM, -LIM, -LIM],
                                  [LIM, LIM, LIM, LIM, LIM, LIM]),
                    options=opts)
    
    return res.x

params = json.loads(open(os.environ['HOME'] + "/Downloads/campdata/nomterr.conf").read())
lat1,lon1=40.250475,28.958810
lat2,lon2=41.224843,29.244813
a0,b0,ex,ey=lon1,lat1,lon2,lat2
connmod = sqlite3.connect(params['elevdbmod'])

latmin = int(np.min([lat1,lat2]))-3
latmax = int(np.max([lat1,lat2]))+3
lonmin = int(np.min([lon1,lon2]))-3
lonmax = int(np.max([lon1,lon2]))+3

lats = list(range(latmin,latmax))
lons = list(range(lonmin,lonmax))

ls = list(itertools.product(lats,lons))

xis, nodes, epsilons = get_rbf_for_latlon_ints(ls,connmod)    

path = find_path(lon1,lat1,lon2,lat2,xis, nodes, epsilons)
print (path)


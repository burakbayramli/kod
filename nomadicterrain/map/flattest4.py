from scipy import optimize
import numpy as np, plot_map, json, os
import geopy.distance, math, route, autograd
from datetime import timedelta
import datetime, sqlite3, pickle, re
#import autograd.numpy as anp

OFFSET = 1.0
DIV = 2.0
alpha = 0.05

params = json.loads(open(os.environ['HOME'] + "/Downloads/campdata/nomterr.conf").read())
        
def dist_matrix(X, Y):
    sx = np.sum(np.power(X,2), 1)
    sy = np.sum(np.power(Y,2), 1)
    D2 =  sx[:, np.newaxis] - np.dot(2.0*X,Y.T) + sy[np.newaxis, :]
    tmp = []
    for x in D2[0]:
        if x>0.0: tmp.append(x)
    D2 = np.array([tmp])    
    D = np.sqrt(D2)
    return D
    
def gaussian(r,eps):
    return np.exp(-np.power((r/eps),2.0))

def f_elev(pts, xis, nodes, epsilons):    
    pts_elevs = {}
    for (lat,lon) in pts:
        if np.isnan(lat) or np.isnan(lon): continue
        #print (lat,lon)
        latm = int(lat)
        lonm = int(lon)            
        lati = int(str(lat).split(".")[1][0])
        lonj = int(str(lon).split(".")[1][0])
        node = nodes[(latm,lonm,lati,lonj)]
        xi = xis[(latm,lonm,lati,lonj)]
        epsilon = epsilons[(latm,lonm,lati,lonj)]
        pts_dist = dist_matrix(np.array([[lat,lon]]), xi.T)        
        elev = np.dot(gaussian(pts_dist, epsilon), node.T)
        #print (elev)
        elev = np.reshape(elev,(len(elev),1))
        pts_elevs[(lat,lon)] = elev[0][0]
    return pts_elevs
   
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
                xis[(latint,lonint,lati,lonj)] = np.array([x for x in rbfi.xi])
                nodes[(latint,lonint,lati,lonj)] = np.array([x for x in rbfi.nodes])
                epsilons[(latint,lonint,lati,lonj)] = np.float(rbfi.epsilon)
                      
    return xis, nodes, epsilons

def trapz(y, dx):
    vals = np.array([_ if np.isnan(_)==False else OFFSET for _ in y[1:-1]])
    #vals = np.array([_ for _ in y[1:-1]])
    tmp = np.sum(vals*2.0)    
    return (y[0]+tmp+y[-1])*(dx/2.0)
    
def find_path(a0,b0,ex,ey,xis,nodes,epsilons):
    t = np.linspace(0,1.0,100)

    cons=({'type': 'ineq','fun': lambda x: 2.0-x[0]}, # y<30
          {'type': 'ineq','fun': lambda x: 2.0-x[1]},
          {'type': 'ineq','fun': lambda x: 2.0-x[2]},
          {'type': 'ineq','fun': lambda x: 2.0-x[3]},
          {'type': 'ineq','fun': lambda x: 2.0-x[4]},
          {'type': 'ineq','fun': lambda x: 2.0-x[5]},
          {'type': 'ineq','fun': lambda x: x[0]}, # y>0
          {'type': 'ineq','fun': lambda x: x[1]},
          {'type': 'ineq','fun': lambda x: x[2]},
          {'type': 'ineq','fun': lambda x: x[3]},
          {'type': 'ineq','fun': lambda x: x[4]},
          {'type': 'ineq','fun': lambda x: x[5]},
    )
    
    def obj(xarg):
        mu = 2.0
        LIM = 2.0
        a1,a2,a3,b1,b2,b3=xarg[0],xarg[1],xarg[2],xarg[3],xarg[4],xarg[5]
        a4 = ex - a0 - (a1+a2+a3)
        b4 = ey - b0 - (b1+b2+b3)
        tmp = b1 + 2*b2*t + 3*b3*np.power(t,2.0) - 112.0*np.power(t,3.0) + np.power((a1 + 2.0*a2*t + 3*a3*np.power(t,2.0) - 65.2*np.power(t,3)),2.0)
        sq = np.sqrt(tmp)
        x = a0 + a1*t + a2*np.power(t,2.0) + a3*np.power(t,3.0) + a4*np.power(t,4.0)
        y = b0 + b1*t + b2*np.power(t,2.0) + b3*np.power(t,3.0) + b4*np.power(t,4.0)
        pts = np.vstack((y,x))
        res = f_elev(pts.T, xis, nodes, epsilons)
        z = np.array(list(res.values()))
        z = np.abs(z)
        #print ('z',z)
        #print ('res',res)
        res = z * sq
        T = trapz(res, 1.0/100.0)

#        T = T - cons
        if ('ArrayBox' not in str(type(T))):
            return float(T)
        return T._value


    np.random.seed(0)
    a1,a2,a3 = np.random.randn()/DIV, np.random.randn()/DIV, np.random.randn()/DIV
    b1,b2,b3 = np.random.randn()/DIV, np.random.randn()/DIV, np.random.randn()/DIV
    x0 = np.array([a1,a2,a3,b1,b2,b3])
    print (x0)
    
    sol = optimize.minimize(obj,
                            x0,
                            method = 'COBYLA',
                            tol=0.01,
                            constraints=cons,
                            options={'disp':True})

    print (sol)

    #0.75757352, 0.27699442, 1.79726859, 1.00155816, 0.92677697,0.33161038
    

def test_obj():
    connmod = sqlite3.connect(params['elevdbmod'])
    lat1,lon1 = 41.084967,31.126588
    lat2,lon2 = 40.749752,31.610694
    ls = [[42,32],[41,32],[42,31],[40,31],[41,30],[41,31],[40,32]]
    xis, nodes, epsilons = get_rbf_for_latlon_ints(ls,connmod)    
    find_path(lon2,lat2,lon1,lat1,xis, nodes, epsilons)

    
#(2, 373)
#(373,)
#0.01
    
test_obj()

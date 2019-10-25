from mpl_toolkits.mplot3d import Axes3D
from scipy.spatial.distance import cdist
from matplotlib.colors import LightSource
from matplotlib import cm
import matplotlib.pyplot as plt


from scipy import optimize
import numpy as np, plot_map, json, os
import geopy.distance, math, route, autograd
from datetime import timedelta
import datetime, sqlite3, pickle, re

OFFSET = 100.0
DIV = 2.0
LIM = 2.0
alpha = 0.05
MAX = 10000

params = json.loads(open(os.environ['HOME'] + "/Downloads/campdata/nomterr.conf").read())
        
def dist_matrix(X, Y):
    sx = np.sum(np.power(X,2), 1)
    sy = np.sum(np.power(Y,2), 1)
    D2 =  sx[:, np.newaxis] - np.dot(2.0*X,Y.T) + sy[np.newaxis, :]
    #tmp = [10000. if x<0.0 else x for x in D2[0]]
    tmp = [x for x in D2[0] if x>0.0 ]
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
        elev = np.reshape(elev,(len(elev),1))        
        pts_elevs[(lat,lon)] = elev[0][0]
    return pts_elevs
   
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
                rbfi = r[0]
                rbfi = pickle.loads(rbfi[0])
                xis[(latint,lonint,lati,lonj)] = np.array([x for x in rbfi.xi])
                nodes[(latint,lonint,lati,lonj)] = np.array([x for x in rbfi.nodes])
                epsilons[(latint,lonint,lati,lonj)] = np.float(rbfi.epsilon)
                      
    return xis, nodes, epsilons

def trapz(y, dx):
    vals = np.array([_ if np.isnan(_)==False else OFFSET for _ in y[1:-1]])
    tmp = np.sum(vals*2.0)    
    return (y[0]+tmp+y[-1])*(dx/2.0)
    
def find_path(a0,b0,ex,ey,xis,nodes,epsilons):
    t = np.linspace(0,1.0,100)

    cons=({'type': 'ineq','fun': lambda x: LIM-x[0]}, # y<LIM
          {'type': 'ineq','fun': lambda x: LIM-x[1]},
          {'type': 'ineq','fun': lambda x: LIM-x[2]},
          {'type': 'ineq','fun': lambda x: LIM-x[3]},
          {'type': 'ineq','fun': lambda x: LIM-x[4]},
          {'type': 'ineq','fun': lambda x: LIM-x[5]},
          {'type': 'ineq','fun': lambda x: x[0]+LIM}, # y>-LIM
          {'type': 'ineq','fun': lambda x: x[1]+LIM},
          {'type': 'ineq','fun': lambda x: x[2]+LIM},
          {'type': 'ineq','fun': lambda x: x[3]+LIM},
          {'type': 'ineq','fun': lambda x: x[4]+LIM},
          {'type': 'ineq','fun': lambda x: x[5]+LIM},
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
        if (len(res)==0): return 100000.
        z = np.array(list(res.values()))
        z[z<0.0] = MAX
        z = z + OFFSET
        #z = np.abs(z)
        res = z * sq
        T = trapz(res, 1.0/100.0)
        return T


    np.random.seed(42)
    a1,a2,a3 = np.random.randn()/DIV, np.random.randn()/DIV, np.random.randn()/DIV
    b1,b2,b3 = np.random.randn()/DIV, np.random.randn()/DIV, np.random.randn()/DIV
    #a1,a2,a3,b1,b2,b3=0.1, 0.1, 1.0, -1.3, 0.0,-0.4
    x0 = np.array([a1,a2,a3,b1,b2,b3])
    print (x0)
    
    sol = optimize.minimize(obj,
                            x0,
                            method = 'COBYLA',
                            tol=0.001,
                            constraints=cons,
                            options={'disp':True})

    print (sol.x)
    return sol.x

def plot_topo_and_pts(lat1,lon1,fout1,how_far,tx,ty):
    D = 30
    boxlat1,boxlon1 = route.goto_from_coord((lat1,lon1), how_far, 45)
    boxlat2,boxlon2 = route.goto_from_coord((lat1,lon1), how_far, 215)

    boxlatlow = np.min([boxlat1,boxlat2])
    boxlonlow = np.min([boxlon1,boxlon2])
    boxlathigh = np.max([boxlat1,boxlat2])
    boxlonhigh = np.max([boxlon1,boxlon2])

    x = np.linspace(boxlonlow,boxlonhigh,D)
    y = np.linspace(boxlatlow,boxlathigh,D)

    xx,yy = np.meshgrid(x,y)
    unique_latlon_ints = {}
    for (x,y) in zip(xx.flatten(),yy.flatten()):
        unique_latlon_ints[int(y),int(x)] = 1

    connmod = sqlite3.connect(params['elevdbmod'])
    k = list(unique_latlon_ints.keys())
    xis, nodes, epsilons = get_rbf_for_latlon_ints(k,connmod)
    
    pts = np.vstack((yy.flatten(),xx.flatten()))
    
    elevs = f_elev(pts.T, xis, nodes, epsilons)

    zz = []
    for (x,y) in zip(xx.flatten(),yy.flatten()):
        zz.append( elevs[(y,x)] )
    
    zz = np.array(zz)
    print (zz.shape)
    zz = zz.reshape(xx.shape)

    plon,plat = np.round(float(lon1),3),np.round(float(lat1),3)

    from scipy.ndimage.filters import gaussian_filter
    sigma = 0.7
    zz = gaussian_filter(zz, sigma)
    
    plt.figure()
    plt.plot(plon,plat,'rd')
    cs=plt.contour(xx,yy,zz,[200,300,400,500,700,900])
    plt.clabel(cs,inline=1,fontsize=9)

    plt.plot(tx,ty,'.')
    
    plt.savefig(fout1)

def test_obj():
    lat1,lon1 = 41.084967,31.126588
    lat2,lon2 = 40.749752,31.610694
    a0,b0,ex,ey=lon2,lat2,lon1,lat1
    connmod = sqlite3.connect(params['elevdbmod'])
    ls = [[42,32],[41,32],[42,31],[40,31],[41,30],[41,31],[40,32]]
    
    xis, nodes, epsilons = get_rbf_for_latlon_ints(ls,connmod)
    
    path = find_path(lon2,lat2,lon1,lat1,xis, nodes, epsilons)

    #a1,a2,a3,b1,b2,b3=0.1, 0.1, 1.0, -1.3, 0.0,-0.4
    #a1,a2,a3,b1,b2,b3=0.60057507,  0.72469955,  1.50004582, -0.30007563,  1.00016673,0.59926283
    #a1,a2,a3,b1,b2,b3=0.60057507,  0.72469955,  1.50004582, -0.30007563,  1.00016673, 0.59926283
    a1,a2,a3,b1,b2,b3=path
    a4 = ex - a0 - (a1+a2+a3)
    b4 = ey - b0 - (b1+b2+b3)
    t = np.linspace(0,1.0,100.0)
    x = a0 + a1*t + a2*np.power(t,2.0) + a3*np.power(t,3.0) + a4*np.power(t,4.0)
    y = b0 + b1*t + b2*np.power(t,2.0) + b3*np.power(t,3.0) + b4*np.power(t,4.0)    
    plot_topo_and_pts(lat2,lon2,'/tmp/out1.png',90.0,x,y)
    
    
test_obj()
#test_plot()

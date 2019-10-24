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

OFFSET = 1.0
DIV = 2.0
alpha = 0.05

params = json.loads(open(os.environ['HOME'] + "/Downloads/campdata/nomterr.conf").read())

def get_elev(pts,connmod):
    cm = connmod.cursor()
    d = {}
    xis = {}
    nodes = {}
    epsilons = {}
    for (lat,lon) in pts:
        latint = str(int(lat))
        lonint = str(int(lon))
        lati = str(lat).split(".")[1][0]
        lonj = str(lon).split(".")[1][0]
        d[(int(latint),int(lonint),int(lati),int(lonj))] = 1
    #print (d)
    for (latint,lonint,lati,lonj) in d.keys():
        sql = "SELECT W from ELEVRBF where latint=? and lonint=? " + \
              "and lati=? and lonj=? " 
        r = cm.execute(sql,(int(latint),int(lonint),int(lati),int(lonj)))
        r = list(r)
        rbfi = r[0]
        rbfi = pickle.loads(rbfi[0])
        xis[(latint,lonint,lati,lonj)] = anp.array([x for x in rbfi.xi])
        nodes[(latint,lonint,lati,lonj)] = anp.array([x for x in rbfi.nodes])
        epsilons[(latint,lonint,lati,lonj)] = rbfi.epsilon
    elevs = f_elev(pts, xis, nodes, epsilons)
    return elevs
    
def get_elev_single(lat,lon,connmod):
    pts = [[lat,lon]]
    connmod = sqlite3.connect(params['elevdbmod'])
    elev = get_elev(pts,connmod)
    return list(elev.values())[0]
    
def dist_matrix(X, Y):
    sx = anp.sum(anp.power(X,2), 1)
    sy = anp.sum(anp.power(Y,2), 1)
    D2 =  sx[:, anp.newaxis] - anp.dot(2.0*X,Y.T) + sy[anp.newaxis, :]
    tmp = []
    for x in D2[0]:
        if x>0.0: tmp.append(x)
    D2 = anp.array([tmp])    
    D = anp.sqrt(D2)
    return D
    
def gaussian(r,eps):
    return anp.exp(-anp.power((r/eps),2.0))

def f_elev(pts, xis, nodes, epsilons):    
    pts_elevs = {}
    for (lat,lon) in pts:
        latm,lonm = None,None
        if 'ArrayBox' in str(type(lat)):
            latm = int(lat._value)
        else:
            latm = int(lat)
        if 'ArrayBox' in str(type(lon)):
            lonm = int(lon._value)
        else:
            lonm = int(lon)
            
        lati = int(str(lat).split(".")[1][0])
        lonj = int(str(lon).split(".")[1][0])
        node = nodes[(latm,lonm,lati,lonj)]
        xi = xis[(latm,lonm,lati,lonj)]
        epsilon = epsilons[(latm,lonm,lati,lonj)]
        pts_dist = dist_matrix(anp.array([[lat,lon]]), xi.T)        
        elev = anp.dot(gaussian(pts_dist, epsilon), node.T)
        #print (elev)
        elev = anp.reshape(elev,(len(elev),1))
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
                xis[(latint,lonint,lati,lonj)] = anp.array([x for x in rbfi.xi])
                nodes[(latint,lonint,lati,lonj)] = anp.array([x for x in rbfi.nodes])
                epsilons[(latint,lonint,lati,lonj)] = anp.float(rbfi.epsilon)
                      
    return xis, nodes, epsilons

def plot_topo(lat1,lon1,fout1,fout2,fout3,how_far):
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
    
    pts = anp.vstack((yy.flatten(),xx.flatten()))
    
    elevs = f_elev(pts.T, xis, nodes, epsilons)

    zz = []
    for (x,y) in zip(xx.flatten(),yy.flatten()):
        zz.append( elevs[(y,x)] )
    
    zz = anp.array(zz)
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
    plt.savefig(fout1)

    fig = plt.figure()
    ax = fig.gca(projection='3d')
    ax.view_init(elev=30,azim=250)
    ax.plot([plon],[plat],[anp.max(zz)],'r.')
    ls = LightSource(270, 45)
    rgb = ls.shade(zz, cmap=cm.gist_earth, vert_exag=0.1, blend_mode='soft')
    surf = ax.plot_surface(xx, yy, zz, rstride=1, cstride=1, facecolors=rgb, linewidth=0, antialiased=False, shade=False)
    plt.savefig(fout2)

    fig = plt.figure()
    ax = fig.gca(projection='3d')
    ax.view_init(elev=30,azim=40)
    ax.plot([plon],[plat],[anp.max(zz)],'r.')
    ls = LightSource(270, 45)
    rgb = ls.shade(zz, cmap=cm.gist_earth, vert_exag=0.1, blend_mode='soft')
    surf = ax.plot_surface(xx, yy, zz, rstride=1, cstride=1, facecolors=rgb, linewidth=0, antialiased=False, shade=False)
    plt.savefig(fout3)

def trapz(y, dx):
    vals = anp.array([_ if anp.isnan(_)==False else OFFSET for _ in y[1:-1]])
    #vals = anp.array([_ for _ in y[1:-1]])
    tmp = anp.sum(vals*2.0)    
    return (y[0]+tmp+y[-1])*(dx/2.0)
    
def find_path(a0,b0,ex,ey,xis,nodes,epsilons):
    t = anp.linspace(0,1.0,100)    
    def obj(xarg):
        mu = 2.0
        LIM = 2.0
        a1,a2,a3,b1,b2,b3=xarg[0],xarg[1],xarg[2],xarg[3],xarg[4],xarg[5]
        a4 = ex - a0 - (a1+a2+a3)
        b4 = ey - b0 - (b1+b2+b3)
        tmp = b1 + 2*b2*t + 3*b3*anp.power(t,2.0) - 112.0*anp.power(t,3.0) + anp.power((a1 + 2.0*a2*t + 3*a3*anp.power(t,2.0) - 65.2*anp.power(t,3)),2.0)
        #print (tmp)
        sq = anp.sqrt(tmp)
        x = a0 + a1*t + a2*anp.power(t,2.0) + a3*anp.power(t,3.0) + a4*anp.power(t,4.0)
        y = b0 + b1*t + b2*anp.power(t,2.0) + b3*anp.power(t,3.0) + b4*anp.power(t,4.0)
        pts = anp.vstack((y,x))
        #print (pts.shape)        
        res = f_elev(pts.T, xis, nodes, epsilons)        
        z = anp.array(list(res.values())) 
        res = z * sq
        T = trapz(res, 1.0/100.0)
        cons = mu * (anp.log(LIM+a1) + anp.log(LIM-a1) + \
                     anp.log(LIM+a2) + anp.log(LIM-a2) + \
                     anp.log(LIM+a3) + anp.log(LIM-a3) + \
                     anp.log(LIM+b1) + anp.log(LIM-b1) + \
                     anp.log(LIM+b2) + anp.log(LIM-b2) + \
                     anp.log(LIM+b3) + anp.log(LIM-b3))
#        T = T - cons
        if ('ArrayBox' not in str(type(T))):
            return float(T)
        return T._value


    anp.random.seed(200)
    a1,a2,a3 = anp.random.randn()/DIV, anp.random.randn()/DIV, anp.random.randn()/DIV
    b1,b2,b3 = anp.random.randn()/DIV, anp.random.randn()/DIV, anp.random.randn()/DIV
    a1,a2,a3,b1,b2,b3=-0.72547412,  0.95547657,  1.35593958, -0.12386914,  0.18073312, -0.01647484
    newx = anp.array([a1,a2,a3,b1,b2,b3])
    print (newx)
    print ('obj',obj(newx))
    
    j = autograd.jacobian(obj)
    J = j(newx)
    print (J)

def test_rbf_get():
    connmod = sqlite3.connect(params['elevdbmod'])
    ls = [[41,30],[41,31]]
    xis, nodes, epsilons = get_rbf_for_latlon_ints(ls,connmod)

def test_dist():
    connmod = sqlite3.connect(params['elevdbmod'])
    xis, nodes, epsilons = get_rbf_for_latlon_ints([[41,31]],connmod)
    lat1,lon1 = 41.084967,31.126588
    res = f_elev(anp.array([[lat1,lon1]]), xis, nodes, epsilons)
    print (res)

def test_topo():
    lat1,lon1 = 41.084967,31.126588
    lat2,lon2 = 40.749752,31.610694
    fout1 = '/tmp/out1.png'
    fout2 = '/tmp/out2.png'
    fout3 = '/tmp/out3.png'
    plot_topo(lat2,lon2,fout1,fout2,fout3,50.0)
    
def pts_elev_test():    
    pts = anp.array([[40.749752,31.610694],[40.749752,31.710694]])
    connmod = sqlite3.connect(params['elevdbmod'])
    print (get_elev_single(40.749752,31.610694,connmod))
    print (get_elev(pts,connmod))

def test_obj():
    connmod = sqlite3.connect(params['elevdbmod'])
    lat1,lon1 = 41.084967,31.126588
    lat2,lon2 = 40.749752,31.610694
    ls = [[41,32],[40,31],[41,30],[41,31]]
    xis, nodes, epsilons = get_rbf_for_latlon_ints(ls,connmod)    
    find_path(lon2,lat2,lon1,lat1,xis, nodes, epsilons)
    
    
#test_dist()
#test_obj()
#test_rbf_get()
#test_topo()
#pts_elev_test()
test_obj()

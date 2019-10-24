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

OFFSET = 1000.0
SROWS = 40000
DIV = 2.0
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

def f_elev2(pts, xis, nodes, epsilons):    
    print (pts.shape)
    pts_elevs = {}
    for (lat,lon) in pts:
        latm = int(lat)
        lonm = int(lon)
        lati = int(str(lat).split(".")[1][0])
        lonj = int(str(lon).split(".")[1][0])
        node = nodes[(latm,lonm,lati,lonj)]
        xi = xis[(latm,lonm,lati,lonj)]
        epsilon = epsilons[(latm,lonm,lati,lonj)]
        pts_dist = dist_matrix(anp.array([[lat,lon]]), xi.T)        
        elev = anp.dot(gaussian(pts_dist, epsilon), node.T)
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
                epsilons[(latint,lonint,lati,lonj)] = rbfi.epsilon
                      
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
    
    pts = np.vstack((yy.flatten(),xx.flatten()))
    
    elevs = f_elev2(pts.T, xis, nodes, epsilons)

    print (elevs)

    exit()

    zz = []
    for (x,y) in zip(xx.flatten(),yy.flatten()):
        zz.append( elevs[str(anp.array([y,x]))] )

    zz = anp.array(zz).reshape(xx.shape)

    plon,plat = np.round(float(lon1),3),np.round(float(lat1),3)

    from scipy.ndimage.filters import gaussian_filter
    sigma = 0.7
    zz = gaussian_filter(zz, sigma)
    
    plt.figure()
    plt.plot(plon,plat,'rd')
    #cs=plt.contour(xx,yy,zz,[100,300,400,500,700,1000,2000])
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


def test_rbf_get():
    connmod = sqlite3.connect(params['elevdbmod'])
    ls = [[41,30],[41,31]]
    xis, nodes, epsilons = get_rbf_for_latlon_ints(ls,connmod)

def test_dist():
    connmod = sqlite3.connect(params['elevdbmod'])
    xis, nodes, epsilons = get_rbf_for_latlon_ints([[41,31]],connmod)
    lat1,lon1 = 41.084967,31.126588
    res = f_elev2(anp.array([[lat1,lon1]]), xis, nodes, epsilons)
    print (res)

def test_topo():
    lat1,lon1 = 41.084967,31.126588
    lat2,lon2 = 40.749752,31.610694
    fout1 = '/tmp/out1.png'
    fout2 = '/tmp/out2.png'
    fout3 = '/tmp/out3.png'
    plot_topo(lat2,lon2,fout1,fout2,fout3,50.0) 
    
#test_dist()
#test_obj()
#test_rbf_get()
test_topo()


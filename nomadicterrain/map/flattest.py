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
mu = 2.0
LIM = 2.0
alpha = 0.05

params = json.loads(open(os.environ['HOME'] + "/Downloads/campdata/nomterr.conf").read())

def do_all_rbf_ints():

    conn = sqlite3.connect(params['elevdb'])
    connmod = sqlite3.connect(params['elevdbmod'])

    c = conn.cursor()
    res = c.execute('''select distinct latint, lonint from elevation; ''')

    for (latint,lonint) in res:
        print ('int---->', latint,lonint)
        sql1 = "SELECT count(*) FROM ELEVATION WHERE latint=%d and lonint=%d; " % (latint,lonint)
        c2 = conn.cursor()
        res1 = c2.execute(sql1)
        res1 = list(res1)
        insert_rbf_recs(latint,lonint,conn,connmod)        
        break

    c.close()
    conn.close()
    connmod.close()

def insert_rbf_recs(latint,lonint,conn,connmod):
    c = conn.cursor()    
    cm = connmod.cursor()    
    sql = "DELETE FROM ELEVRBF where latint=%d and lonint=%d" % (latint, lonint)
    cm.execute(sql)
    connmod.commit()
    for lati in range(10):
        for lonj in range(10):
            print (latint,lati,lonint,lonj)
            sql = "SELECT lat,lon,elevation FROM ELEVATION WHERE latint=%d and lonint=%d " % (latint,lonint)
            res = c.execute(sql)
            X = []; Z=[]
            for (lat,lon,elevation) in res:
                if (".%d"%lati in str(lat)) and \
                   (".%d"%lonj in str(lon)): 
                    X.append([lat,lon])
                    Z.append([elevation])
    
            X = np.array(X)
            Z = np.array(Z)
            print (X.shape)
            if X.shape[0]!=0: 
                rbfi = Rbf(X[:,0], X[:,1], Z,function='gaussian',epsilon=0.01)
                wdf = pickle.dumps(rbfi)
                cm.execute("INSERT INTO ELEVRBF(latint,lonint,lati,lonj,W) VALUES(?,?,?,?,?);",(latint, lonint, lati, lonj, wdf))
                connmod.commit()

def get_elev_single(lat,lon,connmod):
    pts = [[lat,lon]]
    connmod = sqlite3.connect(params['elevdbmod'])
    elev = get_elev(pts,connmod)
    for k in elev.keys(): 
        return elev[k][0]

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
    pts = np.vstack((yy.flatten(),xx.flatten()))
    
    connmod = sqlite3.connect(params['elevdbmod'])
    res = get_pts_rbf(pts.T, connmod)
    elevs = get_elev(pts.T, connmod)

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

    anp.random.seed(20)
    DIV = 2.0
    a1,a2,a3 = anp.random.randn()/DIV, anp.random.randn()/DIV, anp.random.randn()/DIV
    b1,b2,b3 = anp.random.randn()/DIV, anp.random.randn()/DIV, anp.random.randn()/DIV
    #a1,a2,a3,b1,b2,b3=0.2,0.4,0.6,0.6,0.4,0.2
    newx = anp.array([a1,a2,a3,b1,b2,b3])
    print ('newx',newx)
    print ('obj',obj(newx))

    j = autograd.jacobian(obj)
    J = j(newx)
    print (J)


def main_test():    
    lat1,lon1 = 41.084967,31.126588
    lat1 = float(lat1)
    lon1 = float(lon1)
    lat2,lon2 = 40.749752,31.610694
    lat3,lon3 = 40.776241, 31.579548
    connmod = sqlite3.connect(params['elevdbmod'])
    print (get_elev_single(lat2,lon2,connmod))
    #print (get_elev_single(lat3,lon3,connmod))
        
def test_single_rbf_block():
    conn = sqlite3.connect(params['elevdb'])
    connmod = sqlite3.connect(params['elevdbmod'])
    #do_all_rbf_ints()    
    #insert_rbf_recs(40,31,conn,connmod)
    insert_rbf_recs(41,30,conn,connmod)
    insert_rbf_recs(41,31,conn,connmod)
    insert_rbf_recs(40,30,conn,connmod)
    insert_rbf_recs(40,32,conn,connmod)
    insert_rbf_recs(41,32,conn,connmod)

def pts_elev_test():    
    pts = [[40.749752,31.610694],[40.749752,31.710694]]
    connmod = sqlite3.connect(params['elevdbmod'])
    get_elev(pts,connmod)

def test_topo():
    lat1,lon1 = 41.084967,31.126588
    lat2,lon2 = 40.749752,31.610694
    fout1 = '/tmp/out1.png'
    fout2 = '/tmp/out2.png'
    fout3 = '/tmp/out3.png'
    plot_topo(lat2,lon2,fout1,fout2,fout3,10.0) 
    #plot_topo(lat1,lon1,fout1,fout2,fout3,20.0) 
    
def test_obj():
    lat1,lon1 = 41.084967,31.126588
    lat2,lon2 = 40.749752,31.610694
    path_integral(lon1,lat1,lon2,lat2)
    
#test_single_rbf_block()    
#main_test()
#pts_elev_test()
#test_topo()
test_obj()

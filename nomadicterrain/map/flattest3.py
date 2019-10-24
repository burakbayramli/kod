from scipy.spatial.distance import cdist
from scipy.interpolate import Rbf
import numpy as np, json, os
import math, autograd
import datetime, pickle, re
import autograd.numpy as anp

OFFSET = 1.0
DIV = 2.0
alpha = 0.05

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
        latm = int(lat._value)
        lonm = int(lon._value)
            
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
        tmp = b1 + 2*b2*t + 3*b3*anp.power(t,2.0) - 112.0*anp.power(t,3.0) + \
              anp.power((a1 + 2.0*a2*t + 3*a3*anp.power(t,2.0) - \
                         65.2*anp.power(t,3)),2.0)
        #print (tmp)
        sq = anp.sqrt(tmp)
        x = a0 + a1*t + a2*anp.power(t,2.0) + a3*anp.power(t,3.0) + \
            a4*anp.power(t,4.0)
        y = b0 + b1*t + b2*anp.power(t,2.0) + b3*anp.power(t,3.0) + \
            b4*anp.power(t,4.0)
        pts = anp.vstack((y,x))
        res = f_elev(pts.T, xis, nodes, epsilons)        
        z = anp.array(list(res.values())) 
        res = z * sq
        T = trapz(res, 1.0/len(t))
        return T._value

    a1,a2,a3,b1,b2,b3=-0.72547412,  0.95547657,  1.35593958, \
        -0.12386914,  0.18073312, -0.01647484
    newx = anp.array([a1,a2,a3,b1,b2,b3])
    print (newx)
    #print ('obj',obj(newx))
    
    j = autograd.jacobian(obj)
    J = j(newx)
    print (J)

def get_fake_rbfs(latlons):
    xis = {}
    nodes = {}
    epsilons = {}
    for (latint, lonint) in latlons:
        print (latint, lonint)
        for lati in range(10):
            for lonj in range(10):
                xis[(latint,lonint,lati,lonj)] = anp.random.randn(2,373)
                nodes[(latint,lonint,lati,lonj)] = anp.random.randn(373)
                epsilons[(latint,lonint,lati,lonj)] = 0.01
    return xis, nodes, epsilons
    
def test_obj_fake():
    lat1,lon1 = 41.084967,31.126588
    lat2,lon2 = 40.749752,31.610694
    ls = [[41,32],[40,31],[41,30],[41,31]]
    xis, nodes, epsilons = get_fake_rbfs(ls)
    find_path(lon2,lat2,lon1,lat1,xis, nodes, epsilons)
    
test_obj_fake()

import numpy.linalg as lin, datetime
import geopy.distance, sqlite3
from urllib.request import urlopen
import numpy as np, polyline, json
import os, pickle, math, random, requests
import numpy as np, pandas as pd
from constants import gpxbegin
from constants import gpxend
from constants import SROWS
from constants import S
from constants import params
from constants import gps_coord_sample_file
from scipy.optimize import minimize, Bounds, SR1, BFGS

OFFSET = 0.0
LIM = 2.0
MAX = 10000.

def chunks(l, n):
    for i in range(0, len(l), n):
        yield l[i:i + n]

def get_bearing(pointA, pointB):
    lat1 = math.radians(pointA[0])
    lat2 = math.radians(pointB[0])
    
    diffLong = math.radians(pointB[1] - pointA[1])

    x = math.sin(diffLong) * math.cos(lat2)
    y = math.cos(lat1) * math.sin(lat2) - (math.sin(lat1) * math.cos(lat2) * math.cos(diffLong))

    initial_bearing = math.atan2(x, y)

    initial_bearing = math.degrees(initial_bearing)
    compass_bearing = (initial_bearing + 360) % 360

    return np.round(compass_bearing,2)

def goto_from_coord(start, distance, bearing):
    """
    distance: in kilometers
    bearing: 0 degree is north, 90 is east
    """
    s = geopy.Point(start[0],start[1])
    d = geopy.distance.VincentyDistance(kilometers = distance)
    reached = d.destination(point=s, bearing=bearing)
    return [reached.latitude, reached.longitude]


def expand_coords(lat1,lon1,lat2,lon2,margin=5.0):
    res = get_bearing((lat1,lon1),(lat2,lon2))
    print ('bearing before expansion', res)
    if (res >= 0 and res < 90):
        lat3,lon3 = goto_from_coord((lat2,lon2),margin,90)
        lat4,lon4 = goto_from_coord((lat3,lon3),margin,0)        
        lat5,lon5 = goto_from_coord((lat1,lon1),margin,180)
        lat6,lon6 = goto_from_coord((lat5,lon5),margin,270)
    elif (res >= 90 and res < 180):
        lat3,lon3 = goto_from_coord((lat2,lon2),margin,90)
        lat4,lon4 = goto_from_coord((lat3,lon3),margin,180)        
        lat5,lon5 = goto_from_coord((lat1,lon1),margin,270)
        lat6,lon6 = goto_from_coord((lat5,lon5),margin,0)
    elif (res >= 180 and res < 270):
        lat3,lon3 = goto_from_coord((lat2,lon2),margin,180)
        lat4,lon4 = goto_from_coord((lat3,lon3),margin,270)
        lat5,lon5 = goto_from_coord((lat1,lon1),margin,90)
        lat6,lon6 = goto_from_coord((lat5,lon5),margin,0)
    elif (res >= 270 and res < 360):
        lat3,lon3 = goto_from_coord((lat2,lon2),margin,0)
        lat4,lon4 = goto_from_coord((lat3,lon3),margin,270)
        lat5,lon5 = goto_from_coord((lat1,lon1),margin,90)
        lat6,lon6 = goto_from_coord((lat5,lon5),margin,180)
        
    return lat4,lon4,lat6,lon6
        

def get_neighbor_idx(x,y,dims):
    res = []
    for i in ([0,-1,1]):
        for j in ([0,-1,1]):
            if i==0 and j==0: continue
            if x+i<(dims[0]) and x+i>-1 and y+j<(dims[1]) and y+j>-1:
                res.append((x+i,y+j))
    return res
        
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
    
def get_elev_data_ex_chunk(chunk):
    chunk = [list(x) for x in chunk]
    print (chunk[:5])
    data = "["
    for i,x in enumerate(chunk):
        data += str(x)
        if i != len(chunk)-1: data += ","
    data += "]"
    response = requests.post('https://elevation.racemap.com/api',
                             headers={'Content-Type': 'application/json',},
                             data=data)
    res = response.text
    res = res.replace("]","").replace("[","")
    res = res.split(",")
    res = [float(x) for x in res]
    print (res[:5])
    return res
    
def get_elev_int_ex(latint, lonint):
    
    conn = sqlite3.connect(params['elevdb'])
    c = conn.cursor()

    sql1 = "SELECT count(*) FROM ELEVATION WHERE latint=%d and lonint=%d and elevation is NULL" % (latint,lonint)
    res = c.execute(sql1)
    for x in res: print (x)

    sql = "SELECT lat,lon FROM ELEVATION WHERE latint=%d and lonint=%d and elevation is NULL" % (latint,lonint)
    res = c.execute(sql)
    res = list(res)
    N = 40
    for chunk in chunks(res, N):
        elev_results = get_elev_data_ex_chunk(chunk)
        for i in range(N):
            sql = "UPDATE ELEVATION set elevation=%f where lat=%f and lon=%f" % (elev_results[i],chunk[i][0],chunk[i][1])
            c.execute(sql)
        conn.commit()
        res1 = c.execute(sql1)
        for x in res1: print (x)
        print (datetime.datetime.now())

def show_ints():
    conn = sqlite3.connect(params['elevdb'])
    connmod = sqlite3.connect(params['elevdbmod'])
    c = conn.cursor()
    cm = conn.cursor()
    res = c.execute('''select distinct latint, lonint from elevation; ''')
    print (list(res))
    res = cm.execute('''select distinct latint, lonint from rbf1; ''')
    print (list(res))

def gdist(x1,x2):
    x1=x1.reshape(-1,2)
    x2=x2.reshape(-1,2)
    #for x in x1: print (x)
    dists = [geopy.distance.vincenty((a2[0],a1[0]),(a2[1],a1[1])).km for a1,a2 in zip(x1,x2)]
    print (dists)
    return np.array(dists)

def dist_matrix(X, Y):
    sx = np.sum(np.power(X,2), 1)
    sy = np.sum(np.power(Y,2), 1)
    D2 =  sx[:, np.newaxis] - np.dot(2.0*X,Y.T) + sy[np.newaxis, :]
    tmp = [x for x in D2[0] if x>0.0 ]
    D2 = np.array([tmp])    
    D = np.sqrt(D2)
    return D

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
                            
def get_centroid(poly):
    if len(poly) < 3:
        raise ValueError('polygon has less than 3 points')
    for point in poly:
        if type(point) is not list or 2 != len(point):
            raise ValueError('point is not a list of length 2')
    area_total = 0
    centroid_total = [float(poly[0][0]), float(poly[0][1])]
    for i in range(0, len(poly) - 2):
        a, b, c = poly[0], poly[i+1], poly[i+2]
        area = ((a[0] * (b[1] - c[1])) +
                (b[0] * (c[1] - a[1])) +
                (c[0] * (a[1] - b[1]))) / 2.0;
        if 0 == area:
            continue
        centroid = [(a[0] + b[0] + c[0]) / 3.0, (a[1] + b[1] + c[1]) / 3.0]
        centroid_total[0] = ((area_total * centroid_total[0]) +
                             (area * centroid[0])) / (area_total + area)
        centroid_total[1] = ((area_total * centroid_total[1]) +
                             (area * centroid[1])) / (area_total + area)
        area_total += area
    return centroid_total

    
def trapz(y, dx):
    vals = y[1:-1]
    vals = vals[vals>0.0]
    return (y[0]+np.sum(vals*2.0)+y[-1])*(dx/2.0)

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

if __name__ == "__main__":
    #conn = sqlite3.connect(params['elevdb'])
    #connmod = sqlite3.connect(params['elevdbmod'])
    #delete_int_rows(48, 5)
    #show_ints()
    #get_elev_data(42,45)
    do_all_rbf_ints()
    #insert_rbf_recs(40,29,conn,connmod)
    #get_all_countries()

from scipy.spatial.distance import cdist
import numpy.linalg as lin, datetime
import geopy.distance, sqlite3
from urllib.request import urlopen
import numpy as np, polyline, json
import os, pickle, math, random, requests
import numpy as np, pandas as pd
from scipy.interpolate import Rbf
from pqdict import pqdict
from constants import gpxbegin
from constants import gpxend
from constants import SROWS
from constants import S
from constants import params
from constants import gps_coord_sample_file

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

def delete_int_rows(latint, lonint):    
    conn = sqlite3.connect(params['elevdb'])
    c = conn.cursor()
    sql = "DELETE FROM ELEVATION WHERE latint=%d and lonint=%d" % (latint,lonint)
    c.execute(sql)
    conn.commit()
    
def insert_gps_int_rows(latint, lonint):
    gpsidx = np.load(params['coordidx'])
    delete_int_rows(latint, lonint)
    conn = sqlite3.connect(params['elevdb'])
    c = conn.cursor()
    for i,g in enumerate(gpsidx):
        sql = "INSERT INTO ELEVATION(latint,lonint,lat,lon) VALUES(%d,%d,%f,%f);" %(latint,lonint,latint+g[0],lonint+g[1])
        res = c.execute(sql)
        if i%100==0: print (i)
        conn.commit()

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
                rbfi = r[0]
                rbfi = pickle.loads(rbfi[0])
                xis[(latint,lonint,lati,lonj)] = np.array([x for x in rbfi.xi])
                nodes[(latint,lonint,lati,lonj)] = np.array([x for x in rbfi.nodes])
                epsilons[(latint,lonint,lati,lonj)] = np.float(rbfi.epsilon)
                      
    return xis, nodes, epsilons

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
    return list(elev.values())[0]

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
        xis[(latint,lonint,lati,lonj)] = np.array([x for x in rbfi.xi])
        nodes[(latint,lonint,lati,lonj)] = np.array([x for x in rbfi.nodes])
        epsilons[(latint,lonint,lati,lonj)] = rbfi.epsilon
    elevs = f_elev(pts, xis, nodes, epsilons)
    return elevs

def gaussian(r,eps):
    return np.exp(-np.power((r/eps),2.0))

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

def do_all_rbf_ints():
    conn = sqlite3.connect(params['elevdb'])
    connmod = sqlite3.connect(params['elevdbmod'])
    c = conn.cursor()
    #c.execute("delete from RBF1")
    res = c.execute('''select distinct latint, lonint from elevation; ''')

    for (latint,lonint) in res:
        print ('int---->', latint,lonint)
        sql1 = "SELECT count(*) FROM ELEVATION WHERE latint=%d and lonint=%d; " % (latint,lonint)
        c2 = conn.cursor()
        res1 = c2.execute(sql1)
        res1 = list(res1)

        sql2 = "select count(*) from RBF1 where latint=%d and lonint=%d; "  % (latint,lonint)
        c3 = connmod.cursor()
        res2 = c3.execute(sql2)
        res2 = list(res2)

        print(res1[0][0], res2[0][0])

        if res1[0][0]==SROWS and res2[0][0] == 0:
            insert_rbf1_recs(latint,lonint,conn,connmod)
                
        
def get_all_countries():
    print (params['countries'])
    df = pd.read_csv(params['countries'])
    #print (df)
    for row in np.array(df):
        country,longmin,latmin,longmax,latmax =  (row[0],row[2],row[3],row[4],row[5])
        print (country)
        print ('=================================')
        print (longmin,latmin,longmax,latmax)
        for lon in (range(int(longmin),int(longmax)+1)):
            for lat in (range(int(latmin),int(latmax)+1)):
                print (lat,lon)
                get_elev_data(lat,lon,rbf=False)

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



if __name__ == "__main__":
    #conn = sqlite3.connect(params['elevdb'])
    #c = conn.cursor()
    #delete_int_rows(48, 5)
    #show_ints()
    #get_elev_data(42,45)
    #do_all_rbf_ints()
    get_all_countries()

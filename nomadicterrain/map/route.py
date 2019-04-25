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
from constants import gpsidx

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

def dijkstra(C,s,e):    
    D = {}
    P = {}
    Q = pqdict()
    Q[s] = 0

    while len(Q)>0:
        (v,vv) = Q.popitem()
        D[v] = vv
        neighs = get_neighbor_idx(v[0],v[1],C.shape)
        for w in neighs:
            if C[w[0],w[1]] < 0.0: continue # skip negative candidates
            vwLength = D[v] + np.abs(C[v[0],v[1]] - C[w[0],w[1]])
            if w in D:
                if vwLength < D[v]:
                    raise ValueError("error")
            elif w not in Q or vwLength < Q[w]:
                Q[w] = vwLength
                P[w] = v
            
    path = []
    while 1:
       path.append(e)
       if e == s: break
       e = P[e]
    path.reverse()
    return path
        
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
    gpsidx = np.load(params['coordidx'])
    print (len(gpsidx))
    sql = "DELETE FROM ELEVATION WHERE latint=%d and lonint=%d" % (latint,lonint)
    c.execute(sql)
    conn.commit()
    
def insert_gps_int_rows(latint, lonint):
    delete_int_rows(latint, lonint)
    conn = sqlite3.connect(params['elevdb'])
    c = conn.cursor()
    for i,g in enumerate(gpsidx):
        sql = "INSERT INTO ELEVATION(latint,lonint,lat,lon) VALUES(%d,%d,%f,%f);" %(latint,lonint,latint+g[0],lonint+g[1])
        res = c.execute(sql)
        if i%100==0: print (i)
        conn.commit()

def get_elev_data_1(chunk):
    chunk = [list(x) for x in chunk]
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
    return res
    
def get_elev_int(latint, lonint):
    
    conn = sqlite3.connect(params['elevdb'])
    c = conn.cursor()

    sql1 = "SELECT count(*) FROM ELEVATION WHERE latint=%d and lonint=%d and elevation is NULL" % (latint,lonint)
    res = c.execute(sql1)
    for x in res: print (x)

    sql = "SELECT lat,lon FROM ELEVATION WHERE latint=%d and lonint=%d and elevation is NULL" % (latint,lonint)
    res = c.execute(sql)
    res = list(res)
    #N = random.choice([2,40,50,60,70,80])
    N = 40
    print ('N',N)
    for chunk in chunks(res, N):
        elev_results = get_elev_data_1(chunk)
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
    
def insert_rbf1_recs(latint,lonint,conn,connmod):
    df=pd.DataFrame(np.linspace(0,1.0,S))
    df['s'] = df.shift(-1)
    c = conn.cursor()    
    cm = connmod.cursor()    
    sql = "DELETE FROM RBF1 where latint=%d and lonint=%d" % (latint, lonint)
    cm.execute(sql)
    connmod.commit()
    sql = "SELECT lat,lon,elevation FROM ELEVATION WHERE latint=%d and lonint=%d " % (latint,lonint)
    res = list(c.execute(sql)) 
    for i,r1 in enumerate(np.array(df)):
        for j,r2 in enumerate(np.array(df)):
            if j==S-1 or i==S-1: continue
            X = []; Z=[]
            latlow = float(latint)+r1[0]
            lathigh = float(latint)+r1[1]
            lonlow = float(lonint)+r2[0]
            lonhigh = float(lonint)+r2[1]
            print (latlow,lathigh,lonlow,lonhigh)
            for (rlat,rlon,relev) in res:
                if rlat>=latlow and rlat<lathigh and rlon>=lonlow and rlon<lonhigh:
                    X.append([rlon,rlat])
                    Z.append([relev])
            print ('len',len(X))
            X = np.array(X)
            Z = np.array(Z)
            X = X[Z[:,0]>0.0]
            Z = Z[Z[:,0]>0.0]
            if (len(Z)<10): continue
            rbfi = Rbf(X[:,0], X[:,1], Z)
            wdf = pickle.dumps(rbfi)
            cm.execute("INSERT INTO RBF1(latint,lonint,latlow,lathigh,lonlow,lonhigh,W) VALUES(?,?,?,?,?,?,?);",(latint, lonint, latlow, lathigh, lonlow, lonhigh, wdf))
            connmod.commit()
    
def get_elev_single(lat,lon,cm):
    sql = "SELECT latlow,lathigh,lonlow,lonhigh,W from RBF1 where ?>=latlow and ?<lathigh and ?>=lonlow and ?<lonhigh "
    r = cm.execute(sql,(lat,lat,lon,lon))
    r = list(r)
    if len(r)==0: return -10.0
    latlow,lathigh,lonlow,lonhigh,rbfi = r[0]
    rbfi = pickle.loads(rbfi)
    xnew = np.array([[lon,lat]])
    return rbfi(lon, lat)

def get_elev_data_grid_rbf(lat1,lon1,lat2,lon2,c,npts):
    xo,yo = get_grid(lat1,lon1,lat2,lon2,npts=npts)
    start_idx = None
    end_idx = None

    for eps in [0.003, 0.01, 0.1, 1.0]:
        for i in range(xo.shape[0]):
            for j in range(xo.shape[1]):
                if np.abs(xo[i,j]-lat1)<eps and np.abs(yo[i,j]-lon1)<eps:
                    start_idx = (i,j)
                if np.abs(xo[i,j]-lat2)<eps and np.abs(yo[i,j]-lon2)<eps:
                    end_idx = (i,j)
        if start_idx!=None and end_idx != None: break
         
    print ('s',start_idx)
    print ('e',end_idx)

    elev_mat = np.zeros(xo.shape)   
    for i in range(xo.shape[0]):
        for j in range(xo.shape[1]):
            elev_mat[i,j]=get_elev_single(xo[i,j],yo[i,j],c)
    
    return elev_mat, start_idx, end_idx, xo, yo 

def get_elev_data(latint, lonint, rbf=True):
    conn = sqlite3.connect(params['elevdb'])
    connmod = sqlite3.connect(params['elevdbmod'])
    c = conn.cursor()
    sql = "SELECT count(*) FROM ELEVATION WHERE latint=%d and lonint=%d" % (latint,lonint)
    res = c.execute(sql)
    res = list(res)
    print(res)
    if res[0][0]<SROWS:
        print ('inserting')
        insert_gps_int_rows(latint,lonint)
    get_elev_int(latint,lonint)
    if rbf: insert_rbf1_recs(latint,lonint,conn,connmod)

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
    """Calculates the centroid of a non-intersecting polygon.
    Args:
        poly: a list of points, each of which is a list of the form [x, y].
    Returns:
        the centroid of the polygon in the form [x, y].
    Raises:
        ValueError: if poly has less than 3 points or the points are not
                    formatted correctly.
    """
    # Make sure poly is formatted correctly
    if len(poly) < 3:
        raise ValueError('polygon has less than 3 points')
    for point in poly:
        if type(point) is not list or 2 != len(point):
            raise ValueError('point is not a list of length 2')
    # Calculate the centroid from the weighted average of the polygon's
    # constituent triangles
    area_total = 0
    centroid_total = [float(poly[0][0]), float(poly[0][1])]
    for i in range(0, len(poly) - 2):
        # Get points for triangle ABC
        a, b, c = poly[0], poly[i+1], poly[i+2]
        # Calculate the signed area of triangle ABC
        area = ((a[0] * (b[1] - c[1])) +
                (b[0] * (c[1] - a[1])) +
                (c[0] * (a[1] - b[1]))) / 2.0;
        # If the area is zero, the triangle's line segments are
        # colinear so we should skip it
        if 0 == area:
            continue
        # The centroid of the triangle ABC is the average of its three
        # vertices
        centroid = [(a[0] + b[0] + c[0]) / 3.0, (a[1] + b[1] + c[1]) / 3.0]
        # Add triangle ABC's area and centroid to the weighted average
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

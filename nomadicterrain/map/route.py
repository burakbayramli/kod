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
    #connmod = sqlite3.connect(params['elevdbmod'])
    #delete_int_rows(48, 5)
    #get_elev_data(42,45)
    do_all_rbf_ints()
    #insert_rbf_recs(40,29,conn,connmod)
    #get_all_countries()

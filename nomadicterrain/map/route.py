import geopy.distance
from urllib.request import urlopen
import numpy as np, polyline, json
import os, pickle, math
import numpy as np
from pqdict import pqdict

elev_query = "https://maps.googleapis.com/maps/api/elevation/json?locations=enc:%s&key=%s"

gpxbegin = '''<?xml version="1.0" encoding="UTF-8"?>
<gpx creator="Wikiloc - https://www.wikiloc.com" version="1.1"
     xmlns="http://www.topografix.com/GPX/1/1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
<metadata><name>ddddd</name><author><name>ddddd</name>
<link href="https://www.wikiloc.com/wikiloc/user.do?id=1111676">
<text>dddddd</text></link></author><link href="https://www.wikiloc.com/hiking-trails/alanya-oba-kadipinari-cayi-yuruyusu-6911676">
<text>Test1</text></link><time>2014-05-23T08:45:39Z</time></metadata>
<trk>
<name>Test1</name><cmt></cmt><desc>
</desc>
<trkseg>
'''

gpxend = '''
</trkseg>
</trk>
</gpx>
'''

def chunks(l, n):
    for i in range(0, len(l), n):
        yield l[i:i + n]

def get_bearing(lat1,lon1,lat2,lon2):
    dLon = lon2 - lon1;
    y = math.sin(dLon) * math.cos(lat2);
    x = math.cos(lat1)*math.sin(lat2) - math.sin(lat1)*math.cos(lat2)*math.cos(dLon);
    brng = np.rad2deg(math.atan2(y, x));
    brng = 360 - ((brng + 360) % 360)
    return np.round(brng,2)

def goto_from_coord(start, distance, bearing):
    """
    distance: in kilometers
    bearing: 0 degree is north, 90 is east
    """
    s = geopy.Point(start[0],start[1])
    d = geopy.distance.VincentyDistance(kilometers = distance)
    reached = d.destination(point=s, bearing=bearing)
    return [reached.latitude, reached.longitude]


def expand_coords(lat1,lon1,lat2,lon2,margin=3.0):
    res = get_bearing(lat1,lon1,lat2,lon2)
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

def get_elev_data(lat1,lon1,lat2,lon2,npts):
    lat11,lon11,lat22,lon22 = expand_coords(lat1,lon1,lat2,lon2)
    print (lat11,lon11,lat22,lon22)
    xo,yo = get_grid(lat11,lon11,lat22,lon22,npts=npts)
    coords = []
    start_idx = None
    end_idx = None

    for eps in [0.003, 0.01, 0.1, 1.0]:
        for i in range(xo.shape[0]):
            for j in range(xo.shape[1]):
                coords.append((xo[i,j],yo[i,j]))
                if np.abs(xo[i,j]-lat1)<eps and np.abs(yo[i,j]-lon1)<eps:
                    start_idx = (i,j)
                if np.abs(xo[i,j]-lat2)<eps and np.abs(yo[i,j]-lon2)<eps:
                    end_idx = (i,j)
        if start_idx!=None and end_idx != None: break
         
    print ('s',start_idx)
    print ('e',end_idx)

    json_res_results = []
    for c in chunks(coords, 100):
        locs = polyline.encode(c)
        params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())
        url = elev_query % (locs, params['api'])
        html = urlopen(url)
        json_res = json.loads(html.read().decode('utf-8'))
        for i in range(len(json_res['results'])):
            json_res_results.append(json_res['results'][i])
           
    elev_mat = np.zeros(xo.shape)   
    tmp = []
    for i in range(xo.shape[0]*xo.shape[1]):
        tmp.append(json_res_results[i]['elevation'])
    elev_mat = np.array(tmp).reshape(xo.shape)

    return elev_mat, start_idx, end_idx, xo, yo 

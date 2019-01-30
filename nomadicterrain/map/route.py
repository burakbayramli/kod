from urllib.request import urlopen
import numpy as np, polyline, json
import os, pickle
import numpy as np
from pqdict import pqdict

eps = 10e-5

def get_bearing(lat1,lon1,lat2,lon2):
    dLon = lon2 - lon1;
    y = math.sin(dLon) * math.cos(lat2);
    x = math.cos(lat1)*math.sin(lat2) - math.sin(lat1)*math.cos(lat2)*math.cos(dLon);
    brng = np.rad2deg(math.atan2(y, x));
    if brng < 0: brng+= 360
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


def expand_coords(lat1,lon1,lat2,lon2):
    res = get_bearing(lat1,lon1,lat2,lon2)
    if (res >= 0 and res < 90):
        lat3,lon3 = goto_from_coord((lat2,lon2),1,90)
        lat4,lon4 = goto_from_coord((lat3,lon3),1,0)        
        lat5,lon5 = goto_from_coord((lat1,lon1),1,180)
        lat6,lon6 = goto_from_coord((lat5,lon5),1,270)
    elif (res >= 90 and res < 180):
        lat3,lon3 = goto_from_coord((lat2,lon2),1,90)
        lat4,lon4 = goto_from_coord((lat3,lon3),1,180)        
        lat5,lon5 = goto_from_coord((lat1,lon1),1,270)
        lat6,lon6 = goto_from_coord((lat5,lon5),1,0)
    elif (res >= 180 and res < 270):
        lat3,lon3 = goto_from_coord((lat2,lon2),1,180)
        lat4,lon4 = goto_from_coord((lat3,lon3),1,270)
        lat5,lon5 = goto_from_coord((lat1,lon1),1,90)
        lat6,lon6 = goto_from_coord((lat5,lon5),1,0)
    elif (res >= 270 and res < 360):
        lat3,lon3 = goto_from_coord((lat2,lon2),1,0)
        lat4,lon4 = goto_from_coord((lat3,lon3),1,270)
        lat5,lon5 = goto_from_coord((lat1,lon1),1,90)
        lat6,lon6 = goto_from_coord((lat5,lon5),1,180)
        
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

    for v in Q:
        D[v] = Q[v]       
        neighs = get_neighbor_idx(v[0],v[1],C.shape)
        #print (neighs)
        for w in neighs:
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
        
def get_grid(lat1,lon1,lat2,lon2,npts=10):
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


from urllib.request import urlopen
import numpy as np, polyline, json
import os, pickle
from priodict import priorityDictionary
import numpy as np

eps = 10e-5

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
   xo, yo = np.meshgrid(xx,yy,indexing='xy')

   start_idx = None
   if xo[0,0]-lat1<eps and yo[0,0]-lon1<eps:
       start_idx = (0,0)
   elif xo[0,-1]-lat1<eps and yo[0,-1]-lon1<eps:
       start_idx = (0,-1)
   elif xo[-1,-1]-lat1<eps and yo[-1,-1]-lon1<eps:
       start_idx = (-1,-1)
   elif xo[-1,0]-lat1<eps and yo[-1,0]-lon1<eps:
       start_idx = (-1,0)

   return xo,yo,start_idx

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
    Q = priorityDictionary() 
    Q[s] = 0

    for v in Q:
        D[v] = Q[v]
        neighs = get_neighbor_idx(v[0],v[1],C.shape)
        #print (neighs)
        for w in neighs:
            vwLength = D[v] + np.abs(C[v[0],v[1]] - C[w[0],w[1]])
            if w in D:
                if vwLength < D[v]:
                    raise ValueError
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
    
    
#C = np.ones((4,4)) * 999.9
#C[:,-1] = 0.0
#C[-1,:] = 0.0
#print (C)
#p = dijkstra(C,(3,0),(0,3))
#print (p)

if __name__ == "__main__": 
   xo,yo,start_idx = get_grid(36.54,32.0,37.54,33.0)
   print (start_idx)
   print (xo[0,0],yo[0,0])
    
   get_neighbor_idx(3,3,xo.shape)
   #get_neighbor_idx(0,3,xo.shape)
   coords = []
   for i in range(xo.shape[0]):
      for j in range(xo.shape[1]):
         coords.append((xo[i,j],yo[i,j]))
         
   locs = polyline.encode(coords)
   elev_query = "https://maps.googleapis.com/maps/api/elevation/json?locations=enc:%s&key=%s"
   params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())
   url = elev_query % (locs, params['api'])
#   html = urlopen(url)
#   json_res = json.loads(html.read().decode('utf-8'))
#   print (json_res)
#   pickle.dump(json_res,open("/data/data/com.termux/files/home/Downloads/elev.pkl","wb"))

   json_res = pickle.load(open("/data/data/com.termux/files/home/Downloads/elev.pkl","rb"))
   #print (json_res)

   print (xo.shape)
   print (len(json_res['results']))
   
   elev_mat = np.zeros(xo.shape)
   k = 0
   for i in range(xo.shape[0]):
      for j in range(xo.shape[1]):
         elev_mat[i,j] = json_res['results'][k]['elevation']
         k += 1
   print (elev_mat)
   
   find_flattest_path(xo, yo, elev_mat, start_idx)


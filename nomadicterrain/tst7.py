from urllib.request import urlopen
import numpy as np, polyline, json
import os, pickle
from priodict import priorityDictionary
import numpy as np, util
from pqdict import pqdict

eps = 10e-5

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
    #Q = priorityDictionary()
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

#C = np.ones((4,4)) * 999.9
#C[:,-1] = 0.0
#C[-1,:] = 0.0
#print (C)
#p = dijkstra(C,(3,0),(0,3))
#print (p)

if __name__ == "__main__":
   lat1,lon1 = 36.54,32.0
   lat2,lon2 = 36.648548, 32.039130
   
   xo,yo = get_grid(lat1,lon1,lat2,lon2,npts=15)
   #get_neighbor_idx(3,3,xo.shape)
   #get_neighbor_idx(0,3,xo.shape)
   coords = []
   start_idx = None
   end_idx = None
   for i in range(xo.shape[0]):
      for j in range(xo.shape[1]):
         coords.append((xo[i,j],yo[i,j]))
         if np.abs(xo[i,j]-lat1)<eps and np.abs(yo[i,j]-lon1)<eps:
             start_idx = (i,j)
         if np.abs(xo[i,j]-lat2)<eps and np.abs(yo[i,j]-lon2)<eps:
             end_idx = (i,j)

   print ('s',start_idx)
   print ('e',end_idx)
         
   locs = polyline.encode(coords)
   elev_query = "https://maps.googleapis.com/maps/api/elevation/json?locations=enc:%s&key=%s"
   params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())
   url = elev_query % (locs, params['api'])
   html = urlopen(url)
   json_res = json.loads(html.read().decode('utf-8'))
   #print (json_res)
   pickle.dump(json_res,open("/data/data/com.termux/files/home/Downloads/elev.pkl","wb"))

   json_res = pickle.load(open("/data/data/com.termux/files/home/Downloads/elev.pkl","rb"))
   #print (json_res)

   print ('xo',xo.shape)
   print ('len json',len(json_res['results']))
   
   elev_mat = np.zeros(xo.shape)   
   tmp = []
   for i in range(xo.shape[0]*xo.shape[1]):
       tmp.append(json_res['results'][i]['elevation'])
   elev_mat = np.array(tmp).reshape(xo.shape)
   print ('elev start', elev_mat[(0,0)])
   print ('elev end', elev_mat[(15,15)])
   #print (elev_mat)
   #print (xo)
   #print (yo)
#   exit()
   p = dijkstra(elev_mat, start_idx, end_idx)
   #print (p)

   pts = [(xo[c],yo[c]) for c in p]
   elevs = [elev_mat[c] for c in p]

   lines = ""
   lines += util.gpxbegin   
   templ = '<trkpt lat="%f" lon="%f"> <ele>%f</ele></trkpt>\n'
   for c in p:
       lines += templ % (xo[c],yo[c],elev_mat[c])
   lines += util.gpxend
   #print (lines)
   fout = open(params['trails'] + "/01_calc_path.gpx","w")
   fout.write(lines)
   fout.close()
   
   # import sys; sys.path.append("./map")
   # import numpy as np, plot_map, json, os
   # import matplotlib.pyplot as plt
   # import geopy.distance, math, plot_map

   # params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())
   # print (params)

   # #pts = [[42.876171,19.131251],[43.031762, 19.045051],[43.070930, 18.999914], [43.101077, 18.935496], [43.161137, 18.982317], [43.261345, 18.978468], [43.233038, 19.110457], [43.273118, 19.114791], [43.156164, 19.317700], [43.079581, 19.398182], [42.998883, 19.478512], [42.960230, 19.398504], [43.016464, 19.386628], [43.099906, 19.343365], [43.161493, 19.237960], [43.201855, 19.150940], [43.197383, 19.093906], [43.167841, 19.067103], [43.152306, 19.097991], [43.131266, 19.106896], [43.087648, 19.097299], [43.058066, 19.102760], [43.047023, 19.073270], [43.029972, 19.056787], [43.066094, 19.039636], [43.062582, 19.009424], [43.067587, 18.973714]]
   # zfile,scale = params['mapzip']['turkey1']
   # plot_map.plot(pts, 'out.png', zfile=zfile,scale=scale)
   
       

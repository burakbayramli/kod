import numpy as np, plot_map, json, os
import matplotlib.pyplot as plt
import geopy.distance, math, route

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())
print (params)

lat1,lon1 = (36.545471, 31.98567)
lat2,lon2 = (36.07653, 32.836227)    
elev_mat, start_idx, end_idx, xo, yo = route.get_elev_data(lat1,lon1,lat2,lon2,npts=20)
p = route.dijkstra(elev_mat, start_idx, end_idx)
pts = [(xo[c],yo[c]) for c in p]
zfile,scale = params['mapzip']['world1']
plot_map.plot(pts,'out.png',zfile=zfile,scale=scale)


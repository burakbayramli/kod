from mpl_toolkits.mplot3d import Axes3D
from scipy.spatial.distance import cdist
from matplotlib.colors import LightSource
from matplotlib import cm
import os, glob, re, zipfile
import pandas as pd, pickle
import numpy as np, sqlite3, json
import matplotlib.pyplot as plt
from PIL import Image
import geopy.distance, route
import pandas as pd, io

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

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

lat1,lon1 = 42.426010999999995, 18.702119
lat2,lon2 = 42.6597, 18.0894
conn = sqlite3.connect(params['elevdb'])
c = conn.cursor()
elev_mat, start_idx, end_idx, xo, yo = route.get_elev_data_grid_rbf(lat1,lon1,
                                                                    lat2,lon2,
                                                                    c,
                                                                    npts=50)
  
p = route.dijkstra(elev_mat, start_idx, end_idx)

# -*- coding: utf-8 -*-

import pandas as pd
import numpy as np, plot_map, json, os, re
import matplotlib.pyplot as plt, csv
import geopy.distance, math, route, polyline
from scipy.interpolate import interp1d
import math, pyproj

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

lat1,lon1 = [35.323294, 33.308268]

P = pyproj.Proj(proj='utm', zone=31, ellps='WGS84', preserve_units=True)
G = pyproj.Geod(ellps='WGS84')

def LatLon_To_XY(Lat,Lon):
    return P(Lat,Lon)

def XY_To_LatLon(x,y):
    return P(x,y,inverse=True)

x,y = LatLon_To_XY(lat1,lon1)

print (x,y)

lat,lon =  XY_To_LatLon(x,y)

print (lat,lon)




# -*- coding: utf-8 -*-

import pandas as pd
import numpy as np, plot_map, json, os, re
import matplotlib.pyplot as plt, csv
import geopy.distance, math, route, polyline
from scipy.interpolate import interp1d
import math, pyproj

P = pyproj.Proj(proj='utm', zone=33, ellps='WGS84', preserve_units=True)
G = pyproj.Geod(ellps='WGS84')

def LatLon_To_XY(Lat,Lon):
    return P(Lat,Lon)

def XY_To_LatLon(x,y):
    return P(x,y,inverse=True)

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

p = [35.211750,33.327731]

roi = [[35.181427, 32.709977],[35.174166, 32.709248],[35.172846, 32.708602],[35.149015, 32.711502],[35.142647, 32.718818],[35.142063, 32.730244],[35.141613, 32.748601],[35.130131, 32.758803],[35.126641, 32.771738],[35.124647, 32.786174],[35.121018, 32.790978],[35.104505, 32.806946],[35.095936, 32.820727],[35.082761, 32.844722],[35.081995, 32.856632],[35.099895, 32.862908],[35.107085, 32.895099],[35.096610, 32.915753],[35.097312, 32.924078],[35.103562, 32.952317],[35.114507, 32.965561],[35.139098, 32.997404],[35.154744, 33.015581],[35.170338, 33.066844],[35.165776, 33.098207],[35.155521, 33.107026],[35.167593, 33.125345],[35.185588, 33.142677],[35.196549, 33.161004],[35.192716, 33.191227],[35.179190, 33.242640],[35.168122, 33.264377],[35.174967, 33.268075],[35.166071, 33.280662],[35.167074, 33.300349],[35.173511, 33.317082],[35.172438, 33.319851],[35.181013, 33.322062],[35.182365, 33.328852],[35.181973, 33.334145],[35.181295, 33.337947],[35.183235, 33.342540],[35.178896, 33.349509],[35.177969, 33.352058],[35.178901, 33.354153],[35.175475, 33.356506],[35.173986, 33.355485],[35.174266, 33.359390],[35.175476, 33.366145],[35.180224, 33.371924],[35.185861, 33.372260]]

df = pd.DataFrame(roi)
r = np.roll(roi,-1,axis=0)
df[2] = r[:,0]
df[3] = r[:,1]
df.columns = ['y1','x1','y2','x2']
df['d'] = df.apply(lambda r: route.dist_to_seg(r['x1'],r['y1'],r['x2'],r['y2'],p[1],p[0])[0],axis=1)
df['i'] = df.apply(lambda r: route.dist_to_seg(r['x1'],r['y1'],r['x2'],r['y2'],p[1],p[0])[1],axis=1)
lonc,latc = df.ix[df['d'].idxmin(),'i'].flatten()
c = [latc,lonc]

dis = geopy.distance.vincenty(p, (c[0],c[1])).km
b = route.get_bearing(p,(c[0],c[1]))

print (dis,b)

roi.insert(0,[p[0],p[1]])
roi.insert(0,[c[0],c[1]])

zfile,scale = params['mapzip']['turkey3']
plot_map.plot(roi,'/data/data/com.termux/files/home/Downloads/out.png',zfile=zfile,scale=scale)

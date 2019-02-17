# -*- coding: utf-8 -*-
import numpy as np, plot_map, json, os, re
import matplotlib.pyplot as plt, csv
import geopy.distance, math, route, polyline
params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())
                   
locs = u'wqtvEixgjEqKqP{MeD'
locs = polyline.decode(locs)
print (locs)                   

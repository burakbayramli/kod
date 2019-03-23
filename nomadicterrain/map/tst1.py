import numpy as np, plot_map, json, os
import matplotlib.pyplot as plt, quandl
import geopy.distance, math, route
from datetime import timedelta
import datetime
import pandas_datareader.data as web

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())
#print (params)

pts = [[42.425998, 18.702123999999998]]
zfile,scale = params['mapzip']['europe2']
plot_map.plot(pts,'out.png',zfile=zfile,scale=scale)
plt.savefig('/data/data/com.termux/files/home/Downloads/out.png')



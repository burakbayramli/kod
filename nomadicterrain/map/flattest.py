import numpy as np, plot_map, json, os
import geopy.distance, math, route
from datetime import timedelta
import datetime, sqlite3

params = json.loads(open(os.environ['HOME'] + "/Downloads/campdata/nomterr.conf").read())

conn = sqlite3.connect(params['elevdb'])
connmod = sqlite3.connect(params['elevdbmod'])

lat1,lon1 = 41.084967,31.126588
lat1 = float(lat1)
lon1 = float(lon1)
lat2,lon2 = 40.749752,31.610694


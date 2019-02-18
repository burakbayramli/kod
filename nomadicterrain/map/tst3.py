# -*- coding: utf-8 -*-

import pandas as pd
import numpy as np, plot_map, json, os, re
import matplotlib.pyplot as plt, csv
import geopy.distance, math, route, polyline
from scipy.interpolate import interp1d
import math

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

roi = [[35.323294, 33.308268],
       [35.289657, 33.307907],
       [35.323202, 33.373341]
       ]

zfile,scale = params['mapzip']['turkey3']
plot_map.plot(roi,'/data/data/com.termux/files/home/Downloads/out.png',zfile=zfile,scale=scale)





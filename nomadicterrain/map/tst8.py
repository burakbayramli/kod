import numpy as np, sqlite3, json, os
from mpl_toolkits.mplot3d import Axes3D
import matplotlib.pyplot as plt
from matplotlib import cm
import matplotlib.pyplot as plt
import numpy.linalg as lin
from scipy.spatial.distance import cdist
import pandas as pd, pickle


params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

conn = sqlite3.connect(params['elevdb'])
c = conn.cursor()

lat,lon=(36.549177, 31.981221)

sql = "SELECT W, gamma from RBF1 where ?>=latlow and ?<lathigh and ?>=lonlow and ?<lonhigh "
#sql = "SELECT latlow, lathigh, lonlow, lonhigh  from RBF1  "
#sql = "SELECT latlow, lathigh, lonlow, lonhigh from RBF1 where 36.54 >= latlow and 36.54 < lathigh and 31.981221 >= lonlow  "
res = c.execute(sql,(lat,lat,lon,lon))
res = list(res)
if (len(res)==1):
    W,gamma = res[0]
    print (gamma)
    W = pickle.loads(W)
    print (W)


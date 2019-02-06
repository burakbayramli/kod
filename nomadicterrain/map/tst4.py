import numpy as np, sqlite3, json, os
from mpl_toolkits.mplot3d import Axes3D
import matplotlib.pyplot as plt
from matplotlib import cm
import matplotlib.pyplot as plt
import numpy.linalg as lin
from scipy.spatial.distance import cdist

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

conn = sqlite3.connect(params['elevdb'])
c = conn.cursor()

sql = "SELECT lat,lon,elevation FROM ELEVATION WHERE latint=36 and lonint in (31,32) "
res = c.execute(sql)
fout = open("/data/data/com.termux/files/home/Downloads/alanelev.csv","w")
fout.write("lat,lon,elev\n")
for x in res:
    #print (x)
    fout.write("%f,%f,%f\n" % (x[0],x[1],x[2]))
    fout.flush()
fout.close()


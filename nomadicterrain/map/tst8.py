from matplotlib.colors import LightSource
from scipy.spatial.distance import cdist
import numpy as np, sqlite3, json, os
from mpl_toolkits.mplot3d import Axes3D
import matplotlib.pyplot as plt
from matplotlib import cm
import matplotlib.pyplot as plt
import numpy.linalg as lin
import pandas as pd, pickle
import geopy.distance

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

conn = sqlite3.connect(params['elevdb'])
c = conn.cursor()

lat,lon=(36.549177, 31.981221)

sql = "SELECT W, gamma from RBF1 where ?>=latlow and ?<lathigh and ?>=lonlow and ?<lonhigh "
res = c.execute(sql,(lat,lat,lon,lon))
res = list(res)
if (len(res)!=1): raise Exception()
W,gamma = res[0]
print (gamma)
df = pickle.loads(W)
print (df)

#d = df.apply(lambda x:geopy.distance.vincenty((x[1], x[0]),(lat,lon)).km,axis=1)
#df = df[d<10.0]
#print (df)

D=100
xr=np.array(df[0])
xr=xr.reshape(len(xr),1)
yr=np.array(df[1])
yr=yr.reshape(len(xr),1)
X = np.hstack((xr,yr))

x = np.linspace(np.min(xr),np.max(xr),D)
y = np.linspace(np.min(yr),np.max(yr),D)
xx,yy = np.meshgrid(x,y)
xxx = xx.reshape(D*D)
yyy = yy.reshape(D*D)

tmp = np.vstack((xxx,yyy))
d = cdist(X,tmp.T)
print (d)
znew = np.dot(df.w.T,np.exp(-gamma * d)).reshape(D,D)
znew[znew<0] = 0
print (znew.shape)
print (znew)


fig = plt.figure()
ax = fig.gca(projection='3d')
#ax.set_zlim3d(0, 2000)
ax.view_init(elev=30,azim=250)
ls = LightSource(270, 45)
rgb = ls.shade(znew, cmap=cm.gist_earth, vert_exag=0.1, blend_mode='soft')
surf = ax.plot_surface(xx, yy, znew, rstride=1, cstride=1, facecolors=rgb, linewidth=0, antialiased=False, shade=False)
ax.plot([lon],[lat],[1000],'r.')
plt.savefig('/data/data/com.termux/files/home/Downloads/out1.png')

plt.figure()
cs=plt.contour(xx,yy,znew,20)
plt.clabel(cs,inline=1,fontsize=9)
plt.plot(lon,lat,'rd')
plt.savefig('/data/data/com.termux/files/home/Downloads/out2.png')

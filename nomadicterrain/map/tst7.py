import numpy as np

def merc(lat,lon,w=100,h=100):
    x = (lon+180)*(w/360.0)
    latRad = lat*np.pi/180.0;
    mercN = np.log(np.tan((np.pi/4.0)+(latRad/2.0)));
    y     = (h/2.0)-(w*mercN/(2.0*np.pi));
    return x,y

print (merc(36.549177, 31.981221))
print (merc(36.532236, 31.992439))

from mpl_toolkits.mplot3d import Axes3D
import matplotlib.pyplot as plt
from matplotlib import cm
from matplotlib.colors import LightSource
import numpy as np, pandas as pd
import matplotlib.pyplot as plt
import numpy.linalg as lin
from scipy.spatial.distance import cdist

gamma=0.3

df =  pd.read_csv("/data/data/com.termux/files/home/Downloads/alanelev2.csv")
df = df[df.elev > 0.0]

res = df.apply(lambda x: pd.Series(merc(x['lat'],x['lon'])),axis=1)
df1 = pd.DataFrame(res)
df1.columns = ['x','y']
df2 = df.join(df1)
#df2.to_csv('out.csv')

xr=np.array(df2.x)
xr=xr.reshape(len(xr),1)
yr=np.array(df2.y)
yr=yr.reshape(len(xr),1)
zr=np.array(df2.elev)
zr=zr.reshape(len(xr),1)

print (xr.shape)
X = np.hstack((xr,yr))
print (X.shape)
Phi = np.exp(-gamma*cdist(X,X,metric='euclid'))

print (Phi.shape)

w = np.dot(lin.pinv(Phi),zr)
#w = np.dot(lin.pinv(Phi),zr)
w = lin.solve(Phi,zr)

D = 100

x = np.linspace(np.min(xr),np.max(xr),D)
y = np.linspace(np.min(yr),np.max(yr),D)
#x = np.linspace(36,37,D)
#y = np.linspace(31,32,D)
xx,yy = np.meshgrid(x,y)
xxx = xx.reshape(D*D)
yyy = yy.reshape(D*D)

tmp = np.vstack((xxx,yyy))
d = cdist(X,tmp.T)
znew = np.dot(w.T,np.exp(-gamma * d)).reshape(D,D)

fig = plt.figure()
ax = fig.gca(projection='3d')
ax.set_zlim3d(0, 2000)
ax.view_init(elev=30,azim=200)
#surf = ax.plot_surface(xx, yy, znew, cmap=cm.coolwarm, linewidth=0, antialiased=False)
#surf = ax.plot_surface(xx, yy, znew, rstride=1, cstride=1, cmap='viridis', edgecolor='none')
#fig.colorbar(surf, shrink=0.5, aspect=5)

ls = LightSource(270, 45)
rgb = ls.shade(znew, cmap=cm.gist_earth, vert_exag=0.1, blend_mode='soft')
surf = ax.plot_surface(xx, yy, znew, rstride=1, cstride=1, facecolors=rgb, linewidth=0, antialiased=False, shade=False)

plt.savefig('/data/data/com.termux/files/home/Downloads/out5.png')

#            36.549177, 31.981221 me
#            36.532236, 31.992439 mount

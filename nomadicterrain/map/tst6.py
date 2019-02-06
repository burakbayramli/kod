from mpl_toolkits.mplot3d import Axes3D
import matplotlib.pyplot as plt
from matplotlib import cm
import numpy as np, pandas as pd
import matplotlib.pyplot as plt
import numpy.linalg as lin
from scipy.spatial.distance import cdist

gamma=2.0

df =  pd.read_csv("/data/data/com.termux/files/home/Downloads/alanelev2.csv")
df = df[df.elev > 10.0]

xr=np.array(df.lat)
xr=xr.reshape(len(xr),1)
yr=np.array(df.lon)
yr=yr.reshape(len(xr),1)
zr=np.array(df.elev)
zr=zr.reshape(len(xr),1)

print (xr.shape)
X = np.hstack((xr,yr))
print (X.shape)
Phi = np.exp(-gamma*cdist(X,X,metric='euclid'))

print (Phi.shape)

w = np.dot(lin.pinv(Phi),zr)

D = 50

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
ax.view_init(azim=120)
surf = ax.plot_surface(xx, yy, znew, cmap=cm.coolwarm,linewidth=0, antialiased=False)
fig.colorbar(surf, shrink=0.5, aspect=5)
plt.savefig('/data/data/com.termux/files/home/Downloads/out3.png')

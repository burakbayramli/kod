


```python
from mpl_toolkits.mplot3d import Axes3D
import matplotlib.pyplot as plt
from matplotlib import cm

np.random.seed(0)

def func1(x, y):
    s1 = 0.2; x1 = 36.5; y1 = 32.5
    s2 = 0.4; x2 = 36.1; y2 = 32.8
    g1 = np.exp( -4 *np.log(2) * ((x-x1)**2+(y-y1)**2) / s1**2)
    g2 = np.exp( -2 *np.log(2) * ((x-x2)**2+(y-y2)**2) / s2**2)    
    return g1 + g2 

def func2(x, y):
    s1 = 0.2; x1 = 36.5; y1 = 33.5
    s2 = 0.4; x2 = 36.1; y2 = 33.8
    g1 = np.exp( -4 *np.log(2) * ((x-x1)**2+(y-y1)**2) / s1**2)
    g2 = np.exp( -2 *np.log(2) * ((x-x2)**2+(y-y2)**2) / s2**2)    
    return g1 + g2 

S = 50
D = 100

def create_rbfi_hills(latint,lonint):
    x = np.linspace(latint,latint+1,D)
    y = np.linspace(lonint,lonint+1,D)

    xx,yy = np.meshgrid(x,y)
    from scipy.interpolate import Rbf

    xx = xx.reshape(D,D)
    yy = yy.reshape(D,D)
    if lonint==32:
       zz = func1(xx,yy)
    if lonint==33:
       zz = func2(xx,yy)

    idx = np.random.choice(range(D*D),S)
    xr = xx.reshape(D*D)[idx].reshape(S,1)
    yr = yy.reshape(D*D)[idx].reshape(S,1)
    zr = zz.reshape(D*D)[idx].reshape(S,1)

    rbfi = Rbf(xr,yr,zr,function='gaussian',epsilon=0.15)
    return rbfi

edict = {}

edict[(36,32)] = create_rbfi_hills(36,32)
edict[(36,33)] = create_rbfi_hills(36,33)
```

```python
def rbfi_combo(x,y):
    xint = int(x)
    yint = int(y)
    rbfi = edict.get((xint,yint))
    if not rbfi: return 0.0
    return rbfi(x,y)

x = np.linspace(36,37,D)
y = np.linspace(32,34,D)
xx,yy = np.meshgrid(x,y)
zz = [rbfi_combo(xxx,yyy)  for xxx,yyy in zip(xx.flatten(),yy.flatten())]
zz = np.array(zz).reshape(D,D)
```

```python
fig = plt.figure()
ax = fig.gca(projection='3d')
ax.view_init(elev=6	0, azim=120)
surf = ax.plot_surface(xx, yy, zz, cmap=cm.coolwarm,linewidth=0, antialiased=False)
plt.savefig('/tmp/linear_app88rbf_07.png')
```











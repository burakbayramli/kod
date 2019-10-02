
```python
from mpl_toolkits.mplot3d import Axes3D
import matplotlib.pyplot as plt
from matplotlib import cm

np.random.seed(0)

def func(x, y):
    s1 = 0.2; x1 = 36.5; y1 = 32.5
    s2 = 0.4; x2 = 36.1; y2 = 32.8
    g1 = np.exp( -4 *np.log(2) * ((x-x1)**2+(y-y1)**2) / s1**2)
    g2 = np.exp( -2 *np.log(2) * ((x-x2)**2+(y-y2)**2) / s2**2)    
    return g1 + g2 

D = 100

x = np.linspace(36,37,D)
y = np.linspace(32,33,D)

xx,yy = np.meshgrid(x,y)
zz = func(xx,yy)
```

```python
from scipy.interpolate import Rbf

xx = xx.reshape(D,D)
yy = yy.reshape(D,D)
zz = func(xx,yy)

fig = plt.figure()
ax = fig.gca(projection='3d')
ax.view_init(elev=29, azim=29)
surf = ax.plot_surface(xx, yy, zz, cmap=cm.coolwarm,linewidth=0, antialiased=False)
plt.savefig('/tmp/linear_app88rbf_03.png')
```


```python
S = 50
np.random.seed(0)
idx = np.random.choice(range(D*D),S)
xr = xx.reshape(D*D)[idx].reshape(S,1)
yr = yy.reshape(D*D)[idx].reshape(S,1)
zr = zz.reshape(D*D)[idx].reshape(S,1)

rbfi = Rbf(xr,yr,zr,function='gaussian',epsilon=0.15)
```

```python
def dist_matrix(X, Y):
    sx = np.sum(X**2, 1)
    sy = np.sum(Y**2, 1)
    D2 =  sx[:, np.newaxis] - 2.0*X.dot(Y.T) + sy[np.newaxis, :] 
    D2[D2 < 0] = 0
    D = np.sqrt(D2)
    return D
    
test_1 = np.array([[36.0,32.0]])
test_1_dist = dist_matrix(test_1, rbfi.xi.T)
print (test_1_dist.shape)
print (test_1_dist[0][:10])
```

```text
(1, 50)
[0.4229176  1.08927112 0.72276945 0.76827462 0.96299239 1.21064725
 0.85578867 0.94970984 0.80965755 0.76794254]
```

```python
nodes = rbfi.nodes.reshape(1,len(rbfi.nodes))
def gaussian(r,eps): return np.exp(-(r/eps)**2)

def f_interp(newp, rbfi):
    nodes = rbfi.nodes.reshape(1,len(rbfi.nodes))
    newp_dist = dist_matrix(newp, rbfi.xi.T)
    return np.dot(gaussian(newp_dist, rbfi.epsilon), nodes.T)

test_2 = np.array([[36.0,32.0],[36.1,31.9]])
print (f_interp(test_2,rbfi))

test_3 = np.column_stack((xx.ravel(), yy.ravel()))
znewnew = f_interp(test_3,rbfi).reshape(xx.shape)

fig = plt.figure()
ax = fig.gca(projection='3d')
ax.view_init(elev=29, azim=29)
surf = ax.plot_surface(xx, yy, znewnew, cmap=cm.coolwarm,linewidth=0, antialiased=False)
plt.savefig('/tmp/linear_app88rbf_06.png')
```

```text
[[-0.00387063]
 [-0.00337065]]
```







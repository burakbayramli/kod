
```python
from scipy.interpolate import Rbf
import autograd.numpy as anp

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

xx = xx.reshape(D,D)
yy = yy.reshape(D,D)
zz = func(xx,yy)

S = 50
np.random.seed(0)
idx = np.random.choice(range(D*D),S)
xr = xx.reshape(D*D)[idx].reshape(S,1)
yr = yy.reshape(D*D)[idx].reshape(S,1)
zr = zz.reshape(D*D)[idx].reshape(S,1)

rbfi = Rbf(xr,yr,zr,function='gaussian',epsilon=0.15)

def dist_matrix(X, Y):
    sx = anp.sum(X**2, 1)
    sy = anp.sum(Y**2, 1)
    D2 =  sx[:, anp.newaxis] - 2.0*X.dot(Y.T) + sy[anp.newaxis, :] 
    D2[D2 < 0] = 0
    D = anp.sqrt(D2)
    return D
    
test_1 = np.array([[36.0,32.0]])
test_1_dist = dist_matrix(test_1, rbfi.xi.T)

nodes = rbfi.nodes.reshape(1,len(rbfi.nodes))
def gaussian(r,eps): return np.exp(-(r/eps)**2)

def f_interp(newp, rbfi):
    nodes = rbfi.nodes.reshape(1,len(rbfi.nodes))
    newp_dist = dist_matrix(newp, rbfi.xi.T)
    return anp.dot(gaussian(newp_dist, rbfi.epsilon), nodes.T)

test_2 = anp.array([[36.0,32.0],[36.1,31.9]])

print (rbfi.epsilon)
print (rbfi.smooth)
print (rbfi.xi.shape)
print (rbfi.nodes.shape)
print (f_interp(test_2,rbfi))
```

```text
0.15
0.0
(2, 50)
(50,)
[[-0.00387063]
 [-0.00337065]]
```












```python
import json, os
params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())
import plot_map
#pts = [[48.306219, 7.452300]] # near mountain
#pts = [[51.2130605,4.4174822]] # antwerp
#pts = [[40.987659,29.036428],[40.992186,29.039228]] # tr
#pts = [[36.551907, 32.193444]] # alanya park
#pts = [[52.510811, 13.370794]] # pots
#pts = [[36.557532, 32.064841]] # alanya
pts = [[40.267429, 28.946028]] 
#zfile,scale = params['mapzip']['terrain']
#zfile,scale = params['mapzip']['istanbul']
#zfile,scale = params['mapzip']['world2']
zfile = '/home/burak/Downloads/campdata/turkey1.zip'
scale = [1600,-2000]
print (scale)
plot_map.plot(pts,'out.png',zfile=zfile,scale=scale)
```

```text
[1600, -2000]
turkey1/turkey1_map_40_233304620253165_28_97916830379747.png
[40.233304620253165 28.97916830379747]
```


```python
import get_map
#get_map.get_map(48.788836,2.898861, ".", zoom=11)
get_map.get_map(36.525643, 32.090421, ".", zoom=12)
```

```text
http://maps.googleapis.com/maps/api/staticmap?center=36.525643,32.090421&size=800x800&scale=2&maptype=terrain&zoom=12&key=AIzaSyCxQopw-CIBAdyUrhYk18LC_qurTjUWWlE

Out[1]: True
```











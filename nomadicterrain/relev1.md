

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

from scipy.interpolate import Rbf

S = 50
np.random.seed(0)
idx = np.random.choice(range(D*D),S)
xr = xx.reshape(D*D)[idx].reshape(S,1)
yr = yy.reshape(D*D)[idx].reshape(S,1)
zr = zz.reshape(D*D)[idx].reshape(S,1)

rbfi = Rbf(xr,yr,zr,function='gaussian',epsilon=0.15)
```

```python
from autograd import numpy as anp
import autograd

def dist_matrix(X, Y):
    sx = anp.sum(anp.power(X,2), 1)
    sy = anp.sum(anp.power(Y,2), 1)
    D2 =  sx[:, anp.newaxis] - 2.0*anp.dot(X,Y.T) + sy[anp.newaxis, :] 
    #D2[D2 < 0] = 0
    D = anp.sqrt(D2)
    return D

def gaussian(r,eps): return anp.exp(-anp.power((r/eps),2))

def f_interp(newp):
    nodes = rbfi.nodes.reshape(1,len(rbfi.nodes))
    newp_dist = dist_matrix(newp, rbfi.xi.T)
    return anp.dot(gaussian(newp_dist, rbfi.epsilon), nodes.T)

nodes = rbfi.nodes.reshape(1,len(rbfi.nodes))
test_3 = anp.column_stack((xx.ravel(), yy.ravel()))
znewnew = f_interp(test_3).reshape(xx.shape)

fig = plt.figure()
ax = fig.gca(projection='3d')
ax.view_init(elev=29, azim=29)
surf = ax.plot_surface(xx, yy, znewnew, cmap=cm.coolwarm,linewidth=0, antialiased=False)
plt.savefig('/tmp/linear_app88rbf_06.png')
```


```python
def trapz(y, dx):
    #vals = anp.nan_to_num(y[1:-1],1000.0)
    vals = anp.array([_ if anp.isnan(_)==False else 1000.0 for _ in y[1:-1]])
    tmp = anp.sum(vals*2.0)    
    return (y[0]+tmp+y[-1])*(dx/2.0)

t = np.linspace(0,1,100)
a0,b0=(36.0,32.0)
ex,ey=(36.4,33.0)

def intval(a1,a2,a3,b1,b2,b3):
   a4 = ex - a0 - (a1+a2+a3)
   b4 = ey - b0 - (b1+b2+b3)
   sq = anp.sqrt(b1 + 2*b2*t + 3*b3*t**2 - 112.0*t**3 + (a1 + 2*a2*t + 3*a3*t**2 - 65.2*t**3)**2)
   x = a0 + a1*t + a2*t**2 + a3*t**3 + a4*t**4 
   y = b0 + b1*t + b2*t**2 + b3*t**3 + b4*t**4
   z = [f_interp(anp.array([[xx,yy]]))[0][0] for xx,yy in zip(x,y)]
   res = z * sq
   T = trapz(res, 1.0/len(t))
   if 'ArrayBox' in str(type(T)): return T._value
   else: return anp.float(T)

a1,a2,a3 = 1.5, 1.1, 1.0
b1,b2,b3 = 3.9, 1.4, 0.3
T = intval(a1,a2,a3,b1,b2,b3)
print ('T',T)

intval_grad_a1 = autograd.grad(intval,0)
intval_grad_a2 = autograd.grad(intval,1)
intval_grad_a3 = autograd.grad(intval,2)
intval_grad_b1 = autograd.grad(intval,3)
intval_grad_b2 = autograd.grad(intval,4)
intval_grad_b3 = autograd.grad(intval,5)
grad_1 = [intval_grad_a1(a1,a2,a3,b1,b2,b3),\
          intval_grad_a2(a1,a2,a3,b1,b2,b3),\
          intval_grad_a3(a1,a2,a3,b1,b2,b3),\
          intval_grad_b1(a1,a2,a3,b1,b2,b3),\
          intval_grad_b2(a1,a2,a3,b1,b2,b3),\
          intval_grad_b3(a1,a2,a3,b1,b2,b3)
	 ]
print (grad_1)
```

```text
T 50.18249851187272
[-0.11264959569395828, -0.02125323327018775, -0.003483732268591036, 0.023228530294234245, -0.0009775326550836311, -0.0008336455754565775]
```

```python
alpha = 1.0
newx = anp.array([a1,a2,a3,b1,b2,b3])
for i in range(30):
    a1,a2,a3,b1,b2,b3 = newx
    grad_1 = [intval_grad_a1(a1,a2,a3,b1,b2,b3),\
              intval_grad_a2(a1,a2,a3,b1,b2,b3),\
              intval_grad_a3(a1,a2,a3,b1,b2,b3),\
              intval_grad_b1(a1,a2,a3,b1,b2,b3),\
              intval_grad_b2(a1,a2,a3,b1,b2,b3),\
              intval_grad_b3(a1,a2,a3,b1,b2,b3)]
    grad_1 = anp.array(grad_1)
    newx = newx + alpha * grad_1
    print (newx)
    
```

```text
[1.37594505 1.07660607 0.99616841 3.92520916 1.3988234  0.29906077]
[1.2641541  1.05549311 0.99272206 3.94366025 1.39660449 0.29797149]
```

```python
a0,b0=(36.8,32.0)
ex,ey=(36.0,32.8)
a1,a2,a3,b1,b2,b3 = -0.61885097, -0.8231165,  -1.58734855,  3.50903673,  3.04166851,  0.02105481
a4 = ex - a0 - (a1+a2+a3)
b4 = ey - b0 - (b1+b2+b3)
fig = plt.figure()
ax = fig.gca(projection='3d')
ax.view_init(elev=29, azim=29)
surf = ax.plot_wireframe(xx, yy, znewnew, rstride=10, cstride=10)

t = np.linspace(0,1,100)
x = a0 + a1*t + a2*t**2 + a3*t**3 + a4*t**4 
y = b0 + b1*t + b2*t**2 + b3*t**3 + b4*t**4
z = [f_interp(anp.array([[xx,yy]]))[0][0] for xx,yy in zip(x,y)]
ax.plot3D(x, y, z,'r.')

plt.savefig('/tmp/linear_app88rbf_07.png')
```







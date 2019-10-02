import numpy as np, autograd
from autograd import numpy as anp
from scipy import optimize
from mpl_toolkits.mplot3d import Axes3D
import matplotlib.pyplot as plt
from matplotlib import cm

np.random.seed(0)
OFFSET = 10.0

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

def dist_matrix(X, Y):
    sx = np.sum(X**2, 1)
    sy = np.sum(Y**2, 1)
    D2 =  sx[:, np.newaxis] - 2.0*X.dot(Y.T) + sy[np.newaxis, :] 
    D2[D2 < 0] = 0
    D = np.sqrt(D2)
    return D

def gaussian(r,eps): return np.exp(-(r/eps)**2)

def f_interp(x,y, rbfi):
    newp = np.array([[x,y]])
    nodes = rbfi.nodes.reshape(1,len(rbfi.nodes))
    newp_dist = dist_matrix(newp, rbfi.xi.T)
    res = np.dot(gaussian(newp_dist, rbfi.epsilon), nodes.T)
    res = np.float(res[[0]])
    return res+OFFSET

def rbfi_combo(x,y):
    if np.isnan(x): return 0.0
    xint = int(x)
    yint = int(y)
    rbfi = edict.get((xint,yint))
    if not rbfi: return 0.0
    return f_interp(x,y, rbfi)


def trapz(y, dx):
    vals = np.nan_to_num(y[1:-1],0)
    tmp = np.sum(vals*2.0)    
    return (y[0]+tmp+y[-1])*(dx/2.0)

def intval(t,a0,a1,a2,a3,a4,b0,b1,b2,b3,b4):
   sq = np.sqrt(b1 + 2*b2*t + 3*b3*t**2 - 112.0*t**3 + (a1 + 2*a2*t + 3*a3*t**2 - 65.2*t**3)**2)
   x = a0 + a1*t + a2*t**2 + a3*t**3 + a4*t**4 
   y = b0 + b1*t + b2*t**2 + b3*t**3 + b4*t**4
   x = np.array(x)
   y = np.array(y)   
   z = [rbfi_combo(xxx,yyy)  for xxx,yyy in zip(x,y)]
   res = z * sq
   T = trapz(res, 1.0/len(t))
   return T

def find_path(ex,ey,a0,b0):
    
    cons=({'type': 'ineq','fun': lambda x: 30.0-x[0]}, # y<30
	  {'type': 'ineq','fun': lambda x: 30.0-x[1]},
	  {'type': 'ineq','fun': lambda x: 30.0-x[2]},
	  {'type': 'ineq','fun': lambda x: 30.0-x[3]},
	  {'type': 'ineq','fun': lambda x: 30.0-x[4]},
	  {'type': 'ineq','fun': lambda x: 30.0-x[5]},
	  {'type': 'ineq','fun': lambda x: x[0]}, # y>0
	  {'type': 'ineq','fun': lambda x: x[1]},
	  {'type': 'ineq','fun': lambda x: x[2]},
	  {'type': 'ineq','fun': lambda x: x[3]},
	  {'type': 'ineq','fun': lambda x: x[4]},
	  {'type': 'ineq','fun': lambda x: x[5]},
    )

    # baslangic degerleri
    a1,a2,a3 = 0.5,1.0,2.0
    b1,b2,b3 = 0.5,1.0,2.0
    x0 = a1,a2,a3,b1,b2,b3

    def pintval(p):
        a1,a2,a3,b1,b2,b3 = p
        a4 = ex - a0 - (a1+a2+a3)
        b4 = ey - b0 - (b1+b2+b3)   
        t = anp.linspace(0,1,100)
        tmp = b1 + 2.0*b2*t + 3.0*b3*t**2.0 - 112.0*t**3.0 + (a1 + 2.0*a2*t + 3.0*a3*t**2.0 - 65.2*t**3.0)**2.0
        sq = [anp.sqrt(_) if _ != anp.nan else 0.0 for _ in tmp]
        x = a0 + a1*t + a2*t**2.0 + a3*t**3.0 + a4*t**4.0
        y = b0 + b1*t + b2*t**2.0 + b3*t**3.0 + b4*t**4.0
        x = np.array(x)
        y = np.array(y)
        z = np.array([rbfi_combo(xxx,yyy) for xxx,yyy in zip(x,y)])
        res = z * sq
        T = trapz(res, 1.0/len(t))
        print ('T',T)
        return T

    pintval_grad = autograd.grad(pintval)
    
    sol = optimize.minimize(pintval,
			    x0,
			    jac = pintval_grad,
			    method = 'COBYLA',
			    callback=print,
			    tol=0.05,
			    constraints=cons)

    return sol.x

a0,b0=(36.0,32.0)
ex,ey=(36.4,34.0)

res = find_path(ex,ey,a0,b0)
print (res)


import elevutil, uuid
from numpy.linalg import norm
from mpl_toolkits.mplot3d import Axes3D
from matplotlib import cm
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import numpy as np
import requests
import geopy.distance

coords = \
         [[0.,      0.     ],
          [0.05,    0.     ],
          [0.1,     0.5    ],
          [0.15,    0.25   ],
          [0.2,     0.75   ],
          [0.25,    0.125  ],
          [0.3,     0.625  ],
          [0.35,    0.375  ],
          [0.4,     0.875  ],
          [0.45,    0.0625 ],
          [0.5,     0.5625 ],
          [0.55,    0.3125 ],
          [0.6,     0.8125 ],
          [0.65,    0.1875 ],
          [0.7,     0.6875 ],
          [0.75,    0.4375 ],
          [0.8,     0.9375 ],
          [0.85,    0.03125],
          [0.9,     0.53125],
          [0.95,    0.28125]]


def goto_from_coord(start, distance, bearing):
    """
    distance: in kilometers
    bearing: 0 degree is north, 90 is east
    """
    s = geopy.Point(start[0],start[1])
    d = geopy.distance.VincentyDistance(kilometers = distance)
    reached = d.destination(point=s, bearing=bearing)
    return [reached.latitude, reached.longitude]


def get_elev_data(coords):
    chunk = [list(x) for x in coords]
    data = "["
    for i,x in enumerate(chunk):
        data += str(x)
        if i != len(chunk)-1: data += ","
    data += "]"
    response = requests.post('https://elevation.racemap.com/api',
                             headers={'Content-Type': 'application/json',},
                             data=data)
    res = response.text
    res = res.replace("]","").replace("[","")
    res = res.split(",")
    res = [float(x) for x in res]
    return res

def get_topo(lat1,lon1,how_far,fout):
    boxlat1,boxlon1 = elevutil.goto_from_coord((lat1,lon1), how_far, 45)
    boxlat2,boxlon2 = elevutil.goto_from_coord((lat1,lon1), how_far, 215)

    print (boxlat1,boxlon1)
    print (boxlat2,boxlon2)

    latlow = np.min([boxlat1,boxlat2])
    lonlow = np.min([boxlon1,boxlon2])
    lathigh = np.max([boxlat1,boxlat2])
    lonhigh = np.max([boxlon1,boxlon2])

    londiff =(lonhigh-lonlow)
    latdiff =(lathigh-latlow)
    sampleCoords = []
    for c in coords:
        lat = latlow + (c[1] * latdiff)
        lon = lonlow + (c[0] * londiff)
        sampleCoords.append ( [lat, lon])

    sampleCoords = np.array(sampleCoords)
    print (sampleCoords)

    z =  np.array(elevutil.get_elev_data(sampleCoords))
    print (z)

    D = 30
    xx,yy = np.meshgrid(np.linspace(latlow,lathigh,D),
                        np.linspace(lonlow,lonhigh,D))

    n = len(coords)
    sig2 = 0.3 # kernel parameter
    def k(x,u):
        return(np.exp(-0.5*norm(x- u)**2/sig2))
    K = np.zeros((n,n))
    for i in range(n):
        for j in range(n):
            K[i,j] = k(sampleCoords[i,:],sampleCoords[j])
    alpha = np.linalg.solve(K@K.T, K@z) 
    print ('al',alpha)

    N, = xx.flatten().shape
    Kx = np.zeros((n,N))
    for i in range(n):
        for j in range(N):
            Kx[i,j] = k(sampleCoords[i,:],np.array([xx.flatten()[j],yy.flatten()[j]]))

    g = Kx.T @ alpha
    dim = np.sqrt(N).astype(int)
    yhat = g.reshape((dim,dim))
    fig, ax = plt.subplots()
    CS = ax.contour(yy,xx,yhat)
    ax.clabel(CS, inline=1, fontsize=10)
    #plt.savefig('/tmp/topo.png')
    plt.savefig(fout)

if __name__ == "__main__": 
    fout = "/tmp/out-%s.png" % uuid.uuid4()
    lat1,lon1 = 33.810341, 78.781204
    how_far = 200.0
    get_topo(lat1,lon1,how_far,fout)




from scipy.interpolate import NearestNDInterpolator
import ecmwf.data as ecdata
from magpye import GeoMap
from ecmwf.opendata import Client
import pandas as pd
import numpy as np
import simplegeomap as sm
import matplotlib.pyplot as plt

def sliding_window(image, stepSize, windowSize):
  for y in range(0, image.shape[0], stepSize):
    for x in range(0, image.shape[1], stepSize):
      yield np.resize(image[y:y + windowSize[1], x:x + windowSize[0]],windowSize)

def ecmwf_wind(clat,clon,zoom,M=100,N=60,show_ike=False):
    client = Client("ecmwf", beta=True)
    parameters = ['10u', '10v','2t']
    filename = '/tmp/medium-2t-wind.grib'

    client.retrieve(
        date=0,
        time=0,
        step=12,
        stream="oper",
        type="fc",
        levtype="sfc",
        param=parameters,
        target=filename
    )

    data = ecdata.read(filename)

    t2m = data.select(shortName= "2t")
    u = data.select(shortName= "10u")
    v = data.select(shortName= "10v")

    lons = u.longitudes()
    lats = u.latitudes()
    udata = u.values()
    xi = np.linspace(min(lons), max(lons), M)
    yi = np.linspace(min(lats), max(lats), N)
    Xi, Yi = np.meshgrid(xi, yi)
    interp = NearestNDInterpolator(list(zip(lons,lats)), udata)
    uzi = interp(Xi, Yi)

    lons = v.longitudes()
    lats = v.latitudes()
    vdata = v.values()
    xi = np.linspace(min(lons), max(lons), M)
    yi = np.linspace(min(lats), max(lats), N)
    Xi, Yi = np.meshgrid(xi, yi)
    interp = NearestNDInterpolator(list(zip(lons,lats)), vdata)
    vzi = interp(Xi, Yi)

    fig, ax = plt.subplots()
    sm.plot_continents(clat,clon,zoom,incolor='green', outcolor='white', fill=False,ax=ax)
    
    if show_ike:
        W = (3,3)
        # lat,lon 111 kilometers/deg
        lonlen = 111*((min(lons)-max(lons))/M)
        latlen = 111*((min(lats)-max(lats))/M)
        cell_area = latlen * lonlen
        xs = []; ys = []; ikes = []
        for xx,yy,uu,vv in zip(sliding_window(Xi,1,W),sliding_window(Yi,1,W),
                               sliding_window(uzi,1,W),sliding_window(vzi,1,W)):
           u_wind = uu.flatten()
           v_wind = vv.flatten()
           x_coord = xx.flatten()
           y_coord = yy.flatten()
           wspeedsquare = u_wind**2+v_wind**2
           IKE = np.sum(0.5*wspeedsquare*cell_area*W[0]*W[1]) / 1e6
           xs.append(np.mean(x_coord))
           ys.append(np.mean(y_coord))
           ikes.append(IKE)
        
        ikeinterp = NearestNDInterpolator(list(zip(xs,ys)), ikes)
        ikezi = ikeinterp(Xi, Yi)
        plt.pcolormesh(Xi,Yi,ikezi,cmap='Reds')
        
    ax.quiver(Xi,Yi,uzi,vzi)    

if __name__ == "__main__": 
    ecmwf_wind(0,0,18,M=100,N=60,show_ike=True)
    plt.savefig('/tmp/ike.jpg',quality=40)
    

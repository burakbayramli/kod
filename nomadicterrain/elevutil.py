from scipy.interpolate import Rbf
import elevutil, uuid, geopy.distance
from numpy.linalg import norm
from mpl_toolkits.mplot3d import Axes3D
from matplotlib import cm
import numpy as np, folium
import matplotlib.pyplot as plt
import numpy as np, requests

def goto_from_coord(start, distance, bearing):
    """
    distance: in kilometers
    bearing: 0 degree is north, 90 is east
    """
    s = geopy.Point(start[0],start[1])
    d = geopy.distance.distance(kilometers = distance)
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

def get_topo(lat1,lon1,how_far):
    boxlat1,boxlon1 = elevutil.goto_from_coord((lat1,lon1), how_far, 45)
    boxlat2,boxlon2 = elevutil.goto_from_coord((lat1,lon1), how_far, 215)

    latlow = np.min([boxlat1,boxlat2])
    lonlow = np.min([boxlon1,boxlon2])
    lathigh = np.max([boxlat1,boxlat2])
    lonhigh = np.max([boxlon1,boxlon2])

    print (latlow,lathigh,lonlow,lonhigh)
    
    D = 7
    x = np.linspace(lonlow,lonhigh,D)
    y = np.linspace(latlow,lathigh,D)
    xx,yy = np.meshgrid(x,y)
    xxf = xx.reshape(D*D)
    yyf = yy.reshape(D*D)
    sampleCoords = []
    for yyy,xxx in zip(yyf,xxf):
        sampleCoords.append([yyy,xxx])
    sampleCoords = np.array(sampleCoords)
    print (sampleCoords.shape)

    zr =  np.array(elevutil.get_elev_data(sampleCoords))

    yr = sampleCoords[:,0]
    xr = sampleCoords[:,1]

    rbfi = Rbf(xr,yr,zr,function='multiquadric')
    
    D = 15
    x = np.linspace(lonlow,lonhigh,D)
    y = np.linspace(latlow,lathigh,D)
    xx,yy = np.meshgrid(x,y)
    yhat = rbfi(xx,yy)

    fig, ax = plt.subplots()
    CS = ax.contour(xx,yy,yhat)
    return CS

def plot_topo(clat,clon,how_far,fout):
    CS = get_topo(clat,clon,how_far)
    m = folium.Map(location=[clat, clon], zoom_start=14, tiles="Stamen Terrain")
    folium.Marker([clat,clon], icon=folium.Icon(color="green")).add_to(m)
    for i in range(len(CS.allsegs)):
        for li in range(len(CS.allsegs[i])):
            points = []
            x = CS.allsegs[i][li][:,0]
            y = CS.allsegs[i][li][:,1]
            for t, (lon,lat) in enumerate(zip(x,y)):
                points.append([lat,lon]) 
                if t % 3 == 0: 
                    folium.map.Marker(
                        [lat, lon],
                        icon=folium.DivIcon(
                            icon_size=(10,10),
                            icon_anchor=(0,0),
                            html='<div style="color: red; font-size: 8pt">%d</div>' % CS.levels[i])).add_to(m)
            folium.PolyLine(points, color='red', weight=1.0, opacity=1).add_to(m)

    m.save(fout)

if __name__ == "__main__":
    clat,clon=36.64653, 29.13920
    how_far = 50.0
    plot_topo(clat,clon,how_far,"out.html")
    

from pygeodesy.sphericalNvector import LatLon
import elevutil, uuid, geopy.distance, quads
from numpy.linalg import norm
import matplotlib.pyplot as plt
import numpy as np, folium, requests

def cdist(p1,p2):    
    distances = np.linalg.norm(p1 - p2, axis=1)
    return distances

class QuadTreeInterpolator:
    def __init__(self):
        self.tree = quads.QuadTree((0,0), 500, 500)

    def append(self, x, y, z):
        for xx,yy,zz in zip(x,y,z):
            self.tree.insert((xx,yy),data=zz)

    def interp_cell(self, x, y, points):
        a = np.array([x,y]).reshape(-1,2)
        b = np.array(points)[:,:2]
        ds = cdist(a,b)
        ds = ds / np.sum(ds)
        ds = 1. - ds
        c = np.array(points)[:,2]
        iz = np.sum(c * ds) / np.sum(ds)
        return iz
            
    def interpolate(self,x,y):
        res = self.tree.nearest_neighbors((x,y), count=4)
        points = np.array([[c.x, c.y, c.data] for c in res])
        return self.interp_cell(x, y, points)


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

    zr =  np.array(elevutil.get_elev_data2(sampleCoords))

    yr = sampleCoords[:,0]
    xr = sampleCoords[:,1]
    
    q = QuadTreeInterpolator()
    q.append(xr,yr,zr)
    interp = np.vectorize(q.interpolate,otypes=[np.float64])
    
    D = 15
    x = np.linspace(lonlow,lonhigh,D)
    y = np.linspace(latlow,lathigh,D)
    xx,yy = np.meshgrid(x,y)
    yhat = interp(xx,yy)

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
    
def dist(fr,to):
    p1 = LatLon(fr[0], fr[1])
    p2 = LatLon(to[0], to[1])
    return p1.distanceTo(p2)

def bearing(fr,to):
    p1 = LatLon(fr[0], fr[1])
    p2 = LatLon(to[0], to[1])
    return p1.bearingTo(p2)
    
def line_elev_calc(fr, to, fout):
    import matplotlib.pyplot as plt
    npts = 20
    be = bearing(fr, to)
    print (be)
    far = dist(fr,to) / 1000.0
    print (far)
    locs = []
    for x in np.linspace(0,far,npts):
        locs.append(tuple(goto_from_coord([fr[0],fr[1]], x, be)))

    res = get_elev_data2(locs)
    plt.figure()
    plt.plot(np.linspace(0,far,npts),res)
    plt.savefig(fout)

def get_elev_data2(coords):
    url = 'https://api.open-elevation.com/api/v1/lookup?locations='
    for c in coords:
        url += "%s,%s|" % (str(c[0]),str(c[1]))
    res = requests.get(url).json()['results']
    return [r['elevation'] for r in res]

    
def test1():
    clat,clon=36.64653, 29.13920
    how_far = 50.0
    plot_topo(clat,clon,how_far,"/tmp/out.html")

def test2():    
    fout = "/tmp/out-%s.png" % uuid.uuid4()
    line_elev_calc((36.649278935208805, 29.157651665059603), (36.69678555770977, 29.142243855775913), fout)

def test3():
    res = get_elev_data2([[36.649278935208805, 29.157651665059603]])
    print (res)

    
if __name__ == "__main__":
    test1()
    #test2()
    #test3()

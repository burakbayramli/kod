from flask import Flask, render_template
from shapely.geometry import Polygon
import numpy as np, pandas as pd, os, uuid
import sys; sys.path.append("../map")
import plot_map, json, random, geopy.distance

app = Flask(__name__)

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())
print (params)

@app.route('/')
def index():
    return render_template('/index.html')

@app.route('/location/<coordinates>')
def location(coordinates):
    df = pd.read_csv(params['gps'])
    lat,lon = (float(df.tail(1).lat), float(df.tail(1).lon))
    pts = np.array([[lat, lon]]).astype(float)
    fout = "static/out-%s.png" % uuid.uuid4()
    plot_map.plot(pts, fout, params['mapzip'] ) 
    return render_template('/location.html', location=fout)

@app.route('/parks/<coordinates>')
def parks(coordinates):
    df = pd.read_csv(params['gps'])
    lat,lon = (float(df.tail(1).lat), float(df.tail(1).lon))
    pt = np.array([[lat, lon]]).astype(float)
    df = pd.read_csv(params['nationalpark'], sep='|')
    parks = []
    for x in df.index:
        ps = eval(df.ix[x,'Polyline'])
        p = Polygon(ps)
        dist = geopy.distance.vincenty((p.centroid.x,p.centroid.y),(lat,lon))
        if dist.km < float(params['natpark_mindistance']):
            parks.append(ps)                

    fout = "static/out-%s.png" % uuid.uuid4()
    plot_map.plot_area(pt, parks, fout, params['mapzip']) 
    return render_template('/parks.html', location=fout)


@app.route('/camps/<coordinates>')
def camps(coordinates):
    df = pd.read_csv(params['gps'])
    lat,lon = (float(df.tail(1).lat), float(df.tail(1).lon))
    pt = np.array([[lat, lon]]).astype(float)
    df = pd.read_csv(params['campsites'], sep=',')
    df2 = df[['Latitude','Longitude']]
    pts = []
    pts.append([lat,lon])
    for idx in df.index:
        camp = (df2.ix[idx].Latitude, df2.ix[idx].Longitude)
        dist = geopy.distance.vincenty(camp,(lat,lon))
        if dist.km < float(params['natpark_mindistance']):
            pts.append(list(camp))
            
    fout = "static/out-%s.png" % uuid.uuid4()
    plot_map.plot(pts, fout, params['mapzip']) 
    return render_template('/parks.html', location=fout)

if __name__ == '__main__':
    app.debug = True
    app.run(host="localhost", port=5000)

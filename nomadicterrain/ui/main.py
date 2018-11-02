from flask import Flask, render_template, request
import numpy as np, pandas as pd, os, uuid, glob
import sys; sys.path.append("../map")
import plot_map, json, random, geopy.distance

app = Flask(__name__)

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())
print (params)

edible_results = []

class OnlyOne(object):
    class __OnlyOne:
        def __init__(self):
            self.edible = None
        def __str__(self):
            return self.val
    instance = None
    def __new__(cls): # __new__ always a classmethod
        if not OnlyOne.instance:
            OnlyOne.instance = OnlyOne.__OnlyOne()
        return OnlyOne.instance
    def __getattr__(self, name):
        return getattr(self.instance, name)
    def __setattr__(self, name):
        return setattr(self.instance, name)
    
def clean_dir():
    files = glob.glob("static/out-*.png")
    for f in files: os.remove(f)

@app.route('/')
def index():
    return render_template('/index.html')

@app.route('/location/<coordinates>')
def location(coordinates):
    df = pd.read_csv(params['gps'])
    lat,lon = (float(df.tail(1).lat), float(df.tail(1).lon))
    pts = np.array([[lat, lon]]).astype(float)
    fout = "static/out-%s.png" % uuid.uuid4()
    clean_dir()
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
        p_centroid_x,p_centroid_y = plot_map.get_centroid(ps)
        dist = geopy.distance.vincenty((p_centroid_x, p_centroid_y),(lat,lon))
        if dist.km < float(params['natpark_mindistance']):
            parks.append(ps)                

    fout = "static/out-%s.png" % uuid.uuid4()
    clean_dir()
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
            
    clean_dir()
    fout = "static/out-%s.png" % uuid.uuid4()
    plot_map.plot(pts, fout, params['mapzip']) 
    return render_template('/parks.html', location=fout)

@app.route('/edible_main')
def edible_main():
    if 'Dataframe' not in str(type(OnlyOne().edible)):
        OnlyOne().edible = pd.read_csv('/home/burak/Downloads/campdata/edible_plants.csv',sep='|')
    return render_template('/edible.html',data=edible_results)

@app.route("/edible", methods=["POST"])
def edible():
    name = request.form.get("name")

    df = OnlyOne().edible
    res = df[df['Scientific Name'].str.contains("Abies")]['Scientific Name']
    print (res)
    
    #edible_results.append("33333")
    #edible_results.append("44444")
    return edible_main()


if __name__ == '__main__':
    app.debug = True
    app.run(host="localhost", port=5000)

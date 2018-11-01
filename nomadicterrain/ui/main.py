from flask import Flask, render_template
import numpy as np, pandas as pd, os
import sys; sys.path.append("../map")
import plot_map, json, random

app = Flask(__name__)

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())
print (params)

@app.route('/')
def index():
    return render_template('/index.html')

@app.route('/location/<coordinates>')
def location(coordinates):

    df = pd.read_csv(params['gps'])
    print (df.tail(1).lat)
    print (df.tail(1).lon)
    lat,lon = (float(df.tail(1).lat), float(df.tail(1).lon))
    pts = np.array([[lat, lon]]).astype(float)
    fout = "static/out-%d.png" % int(random.random()*1000000)
    plot_map.plot(pts, fout ) 
    return render_template('/location.html', location=fout)

@app.route('/parks/<coordinates>')
def parks(coordinates):
    lat,lon = coordinates.split(";")
    pt = np.array([[lat, lon]]).astype(float)
    df = pd.read_csv(params['national_parks'], sep='|')
    
    for x in df.index:
        print ('inside parks -------------------')
        parks = eval(df.ix[x,'Polyline'])
        print (parks)
                
#    plot_map.plot(pts,'static/out.png')    
    return render_template('/parks.html', location=coordinates)

if __name__ == '__main__':
    app.debug = True
    app.run(host="localhost", port=5000)

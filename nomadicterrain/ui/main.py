from flask import Flask, render_template
import numpy as np
import pandas as pd
import sys; sys.path.append("../map")
import plot_map

app = Flask(__name__)

@app.route('/')
def index():
    return render_template('/index.html')

@app.route('/location/<coordinates>')
def location(coordinates):
    lat,lon = coordinates.split(";")
    pts = np.array([[lat, lon]]).astype(float)
    plot_map.plot(pts,'static/out.png')    
    return render_template('/location.html', location=coordinates)

@app.route('/parks/<coordinates>')
def parks(coordinates):
    lat,lon = coordinates.split(";")
    pt = np.array([[lat, lon]]).astype(float)
    df = pd.read_csv(plot_map.dir + "/" + "national_parks.csv", sep='|')
    
    for x in df.index:
        print ('inside parks -------------------')
        parks = eval(df.ix[x,'Polyline'])
        l = pt + np.array(parks)
        print (l)
                
#    plot_map.plot(pts,'static/out.png')    
    return render_template('/parks.html', location=coordinates)

if __name__ == '__main__':
    app.debug = True
    app.run(host="localhost", port=5000)

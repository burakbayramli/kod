from flask import Flask, render_template
import numpy as np
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
    print (pts)
    plot_map.plot(pts,'static/out.png')    
    return render_template('/location.html', location=coordinates)

if __name__ == '__main__':
    app.debug = True
    app.run(host="localhost", port=5000)

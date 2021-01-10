# -*- coding: utf-8 -*-
from flask import Flask, render_template, request, session
import matplotlib.pyplot as plt, pickle, os
import numpy as np, pandas as pd, os, uuid, glob
import sys; sys.path.append("../map")
import sys; sys.path.append("../guide")
import sys; sys.path.append("..")
import json, random, mindmeld
import geopy.distance, datetime, shutil
import csv, io, zipfile, math, itertools
from urllib.request import urlopen
import urllib, requests, re
import gpxpy, gpxpy.gpx, polyline, codecs
from io import StringIO
import cartopy.crs as ccrs
import cartopy
import util, sqlite3

app = Flask(__name__)

if os.path.isdir("/tmp"): os.environ['TMPDIR'] = "/tmp"

params = json.loads(open(os.environ['HOME'] + "/Downloads/campdata/nomterr.conf").read())

class OnlyOne(object):
    class __OnlyOne:
        def __init__(self):
            self.edible = None
            self.last_location = None
            self.map = "normal"
            self.last_gpx_file = ""
            self.edible_results = []
            self.city_results = []
            self.hay_results = []
            self.poi_results = []
        def __str__(self):
            return self.val
    instance = None
    def __new__(cls): # __new__ always a classmethod
        if not OnlyOne.instance:
            OnlyOne.instance = OnlyOne.__OnlyOne()
            OnlyOne.instance.edible = pd.read_csv(params['edible_plants'],sep='|')
        return OnlyOne.instance
    def __getattr__(self, name):
        return getattr(self.instance, name)
    def __setattr__(self, name):
        return setattr(self.instance, name)
    
def clean_dir():
    files = glob.glob("static/out-*.png")
    for f in files: os.remove(f)

def my_curr_location():
    # take my location from gps logger
    lat,lon = session['geo']
    return lat,lon

@app.route('/')
def index():
    return render_template('/index.html')

@app.route('/location/<loc>')
def location(loc):
    lat,lon = loc.split(';')
    lat,lon=float(lat),float(lon)
    session['geo'] = (lat,lon)
    pts = np.array([[lat, lon]]).astype(float)
    fout = "static/out-%s.png" % uuid.uuid4()
    clean_dir()
    lat,lon=float(lat),float(lon)
    fig = plt.figure()
    ax = fig.add_subplot(1, 1, 1, projection=ccrs.PlateCarree())
    ax.set_global()
    ax.stock_img()
    ax.coastlines()
    ax.plot(lon, lat, 'ro', transform=ccrs.PlateCarree())
    ax.set_extent([lon-0.5, lon+0.5, lat-0.5, lat+0.5])
    plt.savefig(fout)    
    return render_template('/location.html', location=fout, lat=lat, lon=lon)


@app.route('/edible_main')
def edible_main():
    return render_template('/edible.html',data=OnlyOne().edible_results)

@app.route('/edible_detail/<name>')
def edible_detail(name):
    df = OnlyOne().edible
    res = df[df['Scientific Name'].str.lower() == name.lower()]
    print (res)
    res = res.head(1)
    return render_template('/edible_detail.html', name=name, data=list(res.Edibility))

@app.route("/edible", methods=["POST"])
def edible():
    name = request.form.get("name")
    df = OnlyOne().edible
    OnlyOne().edible_results = df[df['Scientific Name'].str.contains(name,case=False)]['Scientific Name'] 
    return edible_main()

@app.route('/profile_main')
def profile_main():
    return render_template('/profile.html')

@app.route("/profile", methods=["POST"])
def profile():
    day = request.form.get("day")
    mon = request.form.get("mon")
    year = request.form.get("year")
    d = "%s%s%s" % (year,mon,day)
    return profile_date(d)

@app.route("/profile_date/<date>", )
def profile_date(date):
    print (date)
    res =  mindmeld.calculate(date)
    res['date'] = date
    if 'spiller_pdf' in params:
        pf = params['spiller_pdf']
        pf = json.loads(open(pf).read())
        res['spiller_tr'] = pf['tr'][res['spiller'].lower()]
        res['spiller_en'] = pf['en'][res['spiller'].lower()]
    return render_template('/profile.html', res=res)

@app.route("/profile_text/<d>")
def profile_text(d):
    res =  mindmeld.calculate(str(d))
    if 'spiller_pdf' in params:
        pf = params['spiller_pdf']
        pf = json.loads(open(pf).read())
        res['spiller_tr'] = pf['tr'][res['spiller'].lower()]
        res['spiller_en'] = pf['en'][res['spiller'].lower()]
    return render_template('/profile_text.html', res=res)

@app.route('/guide/spiller/<which>')
def guide_spiller(which):
    fin = params['guide_detail_dir'] + "/spiller/" + which + ".html"
    output = open(fin).read()
    return render_template('/profile_detail.html', output=output)

@app.route('/guide/chinese/<which>')
def guide_chinese(which):
    fin = params['guide_detail_dir'] + "/chinese/" + which + ".html"
    output = open(fin).read()
    return render_template('/profile_detail.html', output=output)

@app.route('/guide/millman/<which>')
def guide_millman(which):
    fin = params['guide_detail_dir'] + "/millman/" + which + ".txt"
    content = open(fin).readlines()
    output = ""
    for line in content: 
        output += line + "<br/>"
    return render_template('/profile_detail.html', output=output)

@app.route('/guide/lewi/<which>')
def guide_lewi(which):
    fin = params['guide_detail_dir'] + "/lewi/" + which + ".html"
    output = open(fin).read()
    return render_template('/profile_detail.html', output=output)

        
@app.route('/city')
def city():
    return render_template('/city.html',data=OnlyOne().city_results)


@app.route("/city_search", methods=["POST"])
def city_search():
    name = request.form.get("name").lower()
    zfile = params['geocity']
    zip_file    = zipfile.ZipFile(zfile)
    items_file  = zip_file.open('geolitecity.csv')
    items_file  = io.TextIOWrapper(items_file)
    rd = csv.reader(items_file)
    headers = {k: v for v, k in enumerate(next(rd))}
    res = []
    for row in rd:
        if name in row[headers['cityascii2']].lower():
            res.append(row)
    OnlyOne().city_results = res
    return city()

@app.route('/poi')
def poi():
    return render_template('/poi.html',data=OnlyOne().poi_results)

def match(ms, s):
    return not re.search(s,ms,re.IGNORECASE) is None

@app.route("/poi_search", methods=["POST"])
def poi_search():
    keyword = request.form.get("name")
    rd = csv.reader(codecs.open(params['poi'],encoding="utf-8"),delimiter='|')
    headers = {k: v for v, k in enumerate(next(rd))}
    res = []
    lat,lon = my_curr_location()
    for row in rd:
        if match(row[headers['Name']], keyword) or match(row[headers['Type']], keyword): 
            locs = row[headers['Coords']]
            if "[[" in locs:
                locs = eval(locs)
                m = util.get_centroid(locs)
                locs = polyline.encode(locs,precision=6)
            else:                
                locs = eval(locs)
                m = locs
                locs = "%s;%s" % (locs[0],locs[1])

            lat2,lon2 = m
            d = geopy.distance.geodesic((lat2,lon2),(lat, lon))
            if d < 100.0:
                rowname = row[headers['Name']]
                rowdesc = row[headers['Description']]
                rowxx = [row[headers['CoordType']],
                         row[headers['Type']],
                         rowname,rowdesc,locs,np.round(d.km,2)]
                res.append(rowxx)
            
    OnlyOne().poi_results = res
    return poi()

@app.route('/gowind/<loc>')
def gowind(loc):
    lat,lon = loc.split(';')
    lat,lon=float(lat),float(lon)
    print (lat,lon)
    import wind
    lats,lons = wind.get_grid(lat,lon)
    dwind,drain = wind.get_data_multi(lats,lons)
    fout1 = "static/out-%s.png" % uuid.uuid4()
    fout2 = "static/out-%s.png" % uuid.uuid4()
    fout3 = "static/out-%s.png" % uuid.uuid4()
    clean_dir()

    wind.plot_wind(lat, lon, lats, lons, dwind, drain, 0, fout1)
    wind.plot_wind(lat, lon, lats, lons, dwind, drain, 2, fout2)
    wind.plot_wind(lat, lon, lats, lons, dwind, drain, 6, fout3)
        
    return render_template('/wind.html', fout1=fout1, fout2=fout2, fout3=fout3)


@app.route('/gogeo/<coords>')
def gogeo(coords):
    lat,lon = coords.split(';')
    pts = np.array([[lat, lon]]).astype(float)
    fout = "static/out-%s.png" % uuid.uuid4()
    clean_dir()
    lat,lon=float(lat),float(lon)
    fig = plt.figure()
    ax = fig.add_subplot(1, 1, 1, projection=ccrs.PlateCarree())
    ax.set_global()
    ax.stock_img()
    ax.coastlines()
    ax.plot(lon, lat, 'ro', transform=ccrs.PlateCarree())
    ax.set_extent([lon-0.5, lon+0.5, lat-0.5, lat+0.5])
    plt.savefig(fout)    
    return render_template('/location.html', location=fout, lat=lat, lon=lon)

@app.route('/trail/<gpx_file>')
def trail(gpx_file):
    gpx_file2 = open(params['trails'] + "/" + gpx_file)
    gpx = gpxpy.parse(gpx_file2)
    disp = []
    elev_min = 10000
    elev_max = -10000
    total_dist = 0.0
    dists = []
    prev = None
    elevs = False
    lat,lon = None,None
    for track in gpx.tracks:
        for segment in track.segments:
            for point in segment.points:
                lat,lon = point.latitude, point.longitude
                break
            break
        break

    lat2,lon2 = my_curr_location()
    OnlyOne().last_gpx_file = gpx_file
    fout = plot_trail(lat2, lon2, gpx_file, (lat2,lon2))  

    first_point = None
    for track in gpx.tracks:
        for segment in track.segments:
            for i,point in enumerate(segment.points):
                if i==0: first_point = point
                if prev:
                    prev_dist = geopy.distance.geodesic((point.latitude, point.longitude),(prev.latitude,prev.longitude))
                    total_dist +=  prev_dist.km
                if point.elevation:
                    elevs = True
                    if point.elevation < elev_min: elev_min = point.elevation
                    if point.elevation > elev_max: elev_max = point.elevation
                d = geopy.distance.geodesic((point.latitude, point.longitude),(lat,lon))
                dists.append([point, d.km])
                prev = point

    disp.append("Total distance %f " % total_dist)
    disp.append("Total elevation %f " % (elev_max-elev_min))
    dists = np.array(dists)
    start_idx = dists[:,1].argmin()

    front = [0.1, 0.2, 10000.0]
    elev_mins = [10000.0, 10000.0, 10000.0]
    elev_maxs = [-10000.0, -10000.0, -10000.0]
    curr_dist= float(0.0)
    prev = None
    if elevs:
       for i in range(start_idx, len(dists)):    
           if prev: curr_dist += (geopy.distance.geodesic((dists[i][0].latitude,
                                                           dists[i][0].longitude),
                                                          (prev.latitude,
                                                           prev.longitude))).km
           for j in range(len(front)):
               if curr_dist < front[j]:
                   if dists[i][0].elevation < elev_mins[j]: elev_mins[j] = dists[i][0].elevation
                   if dists[i][0].elevation > elev_maxs[j]: elev_maxs[j] = dists[i][0].elevation
           prev = dists[i][0] 

       disp.append("For the next x km the elevation change will be")
       for j in range(len(front)):
           disp.append("%f %f" % (front[j], elev_maxs[j]-elev_mins[j]))
           #print (front[j], elev_maxs[j]-elev_mins[j])
    
    return render_template('/trail.html', location=fout, disp=disp, link=gpx.link, first_point_lat=first_point.latitude, first_point_lon=first_point.longitude)

def plot_trail(lat, lon, gpx_file, my_curr_location):
    pts = []
    gpx_file = open(params['trails'] + "/" + gpx_file)
    gpx = gpxpy.parse(gpx_file)
    for track in gpx.tracks:
        for segment in track.segments:
            for point in segment.points:
                pts.append([point.latitude, point.longitude])

    clean_dir()
    fout = "static/out-%s.png" % uuid.uuid4()

    fig = plt.figure()
    ax = fig.add_subplot(1, 1, 1, projection=ccrs.PlateCarree())
    ax.set_global()
    ax.stock_img()
    ax.coastlines()
    ax.plot(lon, lat, 'rx', transform=ccrs.PlateCarree())

    lats = [x[0] for x in pts]
    lons = [x[1] for x in pts]

    ax.plot(lons, lats, 'r.', transform=ccrs.PlateCarree())

    EXT = 1.5
    ax.set_extent([lon-EXT, lon+EXT, lat-EXT, lat+EXT])
    plt.savefig(fout)    
    
    return fout

@app.route('/trails')
def trails():
    res = []
    files = glob.glob(params['trails'] + "/*.gpx" )
    lat2,lon2 = my_curr_location()
    for x in files:
        gpx_file = open(x)
        gpx = gpxpy.parse(gpx_file)
        for track in gpx.tracks:
            for segment in track.segments:
                for point in segment.points:
                    lat,lon = point.latitude, point.longitude
                    d = geopy.distance.geodesic((lat2,lon2),(lat,lon)).km
                    break
                break
            break
        
        res.append([x[x.rindex('/')+1:],np.round(d,2)])
    
    return render_template('/trails.html', res=res)

@app.route('/hay')
def hay():
    return render_template('/hay.html',data=OnlyOne().hay_results)

@app.route("/hay_search", methods=["POST"])
def hay_search():    
    name = request.form.get("name").lower()
    hayf = params['hay'] + "/hay.txt"
    content = open(hayf).read()
    content = content.replace("\n-"," -")
    sio = StringIO(content)
    res = []
    for line in sio.readlines():
        tokens = line.split(":")
        if name in tokens[0].lower(): 
            res.append(line.replace("-","\n-"))
    
    OnlyOne().hay_results = res
    return hay()

    
@app.route('/gopoly/<coords>')
def gopoly(coords):
    locs = polyline.decode(coords,precision=6)
    locs = [list(x) for x in locs]
    print ('locs',locs)
    
    lat2,lon2 = my_curr_location()
    
    fout = "static/out-%s.png" % uuid.uuid4()
    clean_dir()

    fig = plt.figure()
    ax = fig.add_subplot(1, 1, 1, projection=ccrs.PlateCarree())
    ax.set_global()
    ax.stock_img()
    ax.coastlines()
    lats = [x[0] for x in locs]
    lons = [x[1] for x in locs]
    ax.plot(lon2, lat2, 'rx', transform=ccrs.PlateCarree())
    ax.plot(lons, lats, 'r.', transform=ccrs.PlateCarree())
    lat=lats[0]
    lon=lons[0]
    EXT = 0.5
    ax.set_extent([lon-EXT, lon+EXT, lat-EXT, lat+EXT])
    plt.savefig(fout)    
    
    return render_template('/poly.html', location=fout)

if __name__ == '__main__':
    app.debug = True
    app.secret_key = "aksdfkasf"
    app.run(host="localhost",port=5000)
       

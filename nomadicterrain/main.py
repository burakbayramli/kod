# -*- coding: utf-8 -*-
from flask import Flask, render_template, request
import matplotlib.pyplot as plt, pickle
import numpy as np, pandas as pd, os, uuid, glob
import sys; sys.path.append("../map")
import sys; sys.path.append("../guide")
import sys; sys.path.append("../loogle")
import sys; sys.path.append("..")
import plot_map, json, random, mindmeld
import geopy.distance, datetime, shutil
import csv, io, zipfile, math, itertools
from urllib.request import urlopen
import urllib, requests, json, re, youtube_dl
import gpxpy, gpxpy.gpx, polyline, codecs
from io import StringIO
import route, sqlite3, datedelta
from datetime import timedelta
import pandas_datareader.data as web, loogle3
import quandl, os, calendar, timezonefinder
from pytz import timezone
from bs4 import BeautifulSoup
import urllib.request as req2

app = Flask(__name__)

if os.path.isdir("/tmp"): os.environ['TMPDIR'] = "/tmp"

params = json.loads(open(os.environ['HOME'] + "/Downloads/campdata/nomterr.conf").read())

place_query = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%s&radius=%d&type=%s&keyword=%s&key=%s"

place_query2 = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%s&radius=10000&keyword=&type=%s&key=%s"


def been_walking():
    df = pd.read_csv(params['gps'])
    df1 = df.iloc[::-1]
    df1['lat1'] = df1.lat.shift(-1)
    df1['lon1'] = df1.lon.shift(-1)
    df1 = df1.fillna(0)
    df1.loc[:,'dist'] = df1.apply(lambda x: geopy.distance.vincenty((x.lat1, x.lon1),(x.lat,x.lon)).km, axis=1)
    df1['dists'] = df1.dist.cumsum()

    mylat, mylon = my_curr_location()
    
    res = {}
    for idx in df1.index:
        currd = int(df1.loc[idx,'dists']*1000.0)
        if currd > 1000.0: break
        if currd-1000 < 100:
            res['1000'] = route.get_bearing((float(df1.loc[idx,'lat']),
                                            float(df1.loc[idx,'lon'])),
                                            (mylat,mylon))
        if currd-200 <60:
            res['200'] = route.get_bearing((float(df1.loc[idx,'lat']),
                                           float(df1.loc[idx,'lon'])),
                                           (mylat,mylon))
        if currd-100 < 20:
            res['100'] = route.get_bearing((float(df1.loc[idx,'lat']),
                                           float(df1.loc[idx,'lon'])),
                                           (mylat,mylon))
        if currd-50 < 30:
            res['50'] = route.get_bearing((float(df1.loc[idx,'lat']),
                                          float(df1.loc[idx,'lon'])),
                                          (mylat,mylon))
        if currd-10 < 5:
            res['10'] = route.get_bearing((float(df1.loc[idx,'lat']),
                                          float(df1.loc[idx,'lon'])),
                                          (mylat, mylon))
    return res
    

class OnlyOne(object):
    class __OnlyOne:
        def __init__(self):
            self.edible = None
            self.last_location = None
            self.map = "normal"
            self.last_gpx_file = ""
            self.edible_results = []
            self.city_results = []
            self.celeb_results = []
            self.place_results = []
            self.line_elev_results = []
            self.hay_results = []
            self.poi_results = []
            self.book_results = []
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
    df = pd.read_csv(params['gps'])
    return float(df.tail(1).lat), float(df.tail(1).lon)

def my_curr_elevation():
    df = pd.read_csv(params['gps'])
    return np.round(float(df.tail(1).elevation),2)

@app.route('/')
def index():
    loc = str(my_curr_location())
    elev = str(my_curr_elevation())
    return render_template('/index.html', loc=loc,elev=elev)

@app.route('/location')
def location():
    lat,lon = my_curr_location()
    pts = np.array([[lat, lon]]).astype(float)
    fout = "static/out-%s.png" % uuid.uuid4()
    clean_dir()
    OnlyOne().last_location = [lat,lon]
    map = OnlyOne().map
    zfile,scale = params['mapzip'][map]
    plot_map.plot(pts, fout, zfile=zfile, scale=scale)
    walking = been_walking()
    elev = get_elev(lat,lon)
    return render_template('/location.html', location=fout, walking=walking, lat=lat, lon=lon, elev=elev)


@app.route('/edible_main')
def edible_main():
    return render_template('/edible.html',data=OnlyOne().edible_results)

@app.route('/food_main')
def food_main():
    return render_template('/food.html')

@app.route('/btype_main')
def btype_main():
    return render_template('/btype.html')

@app.route('/btype/<type>')
def btype_detail(type):
    d = params['btype']
    df = pd.read_csv(d + '/food.dat',sep=';')
    res = []
    if type=='a':
        res = np.array(df.ix[:, ['Dadamo_Site_Id','Food','A_S','A_NS']])
    if type=='b':
        res = np.array(df.ix[:, ['Dadamo_Site_Id','Food','B_S','B_NS']])
    if type=='O':
        res = np.array(df.ix[:, ['Dadamo_Site_Id','Food','O_S','O_NS']])
    if type=='ab':
        res = np.array(df.ix[:, ['Dadamo_Site_Id','Food','AB_S','AB_NS']])
    return render_template('/btype.html', res=res)

@app.route('/edible_detail/<name>')
def edible_detail(name):
    df = OnlyOne().edible
    res = df[df['Scientific Name'].str.lower() == name.lower()]
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

def step(request, location, distance):
    if request.form['action'] == '↑':
        res = route.goto_from_coord(OnlyOne().last_location,
                                    distance,
                                    0)
        
    elif request.form['action'] == '↓':
        res = route.goto_from_coord(OnlyOne().last_location,
                                    distance,
                                    180)
    elif request.form['action'] == '→':
        res = route.goto_from_coord(OnlyOne().last_location,
                                    distance,
                                    90)
    elif request.form['action'] == '←':
        res = route.goto_from_coord(OnlyOne().last_location,
                                    float(request.form['distance']),
                                    270)
    return res
    
@app.route('/location_nav_action', methods=['GET', 'POST'])
def location_nav_action():
    res = step(request, OnlyOne().last_location, float(request.form['distance']))    
    pts = np.array([[res[0], res[1]]]).astype(float)
    fout = "static/out-%s.png" % uuid.uuid4()
    clean_dir()
    OnlyOne().last_location = res
    map = OnlyOne().map
    zfile,scale = params['mapzip'][map]
    plot_map.plot(pts, fout, zfile=zfile,scale=scale ) 
    return render_template('/location.html', location=fout, lat=res[0],lon=res[1])

@app.route('/parks_nav_action', methods=['GET', 'POST'])
def parks_nav_action():
    res = step(request, OnlyOne().last_location, float(request.form['distance']))    
    OnlyOne().last_location = res
    fout = plot_parks(res[0], res[1])
    print (fout)
    return render_template('/parks.html', location=fout)

@app.route('/camps_nav_action', methods=['GET', 'POST'])
def camps_nav_action():
    res = step(request, OnlyOne().last_location, float(request.form['distance']))    
    OnlyOne().last_location = res
    fout,names = plot_camps(res[0], res[1])
    print (fout)
    return render_template('/camps.html', location=fout, names=names)

@app.route('/trace_nav_action', methods=['GET', 'POST'])
def trace_nav_action():
    res = step(request, OnlyOne().last_location, float(request.form['distance']))    
    OnlyOne().last_location = res
    df = pd.read_csv(params['gps'])
    pts = np.flip(np.array(df[['lat','lon']]), axis=0)
    pts[0] = res
    fout = plot_trace(pts)
    print (fout)
    return render_template('/trace.html', location=fout)

@app.route('/mapset')
def mapset():
    return render_template('/mapset.html',
                           maps=params['mapzip'].keys(),
                           map=OnlyOne().map)

@app.route("/choosemap", methods=["GET","POST"])
def choosemap():
    map = request.form['option']
    OnlyOne().map = map
    print (map)
    return mapset()

def plot_trace(pts):
    fout = "static/out-%s.png" % uuid.uuid4()
    clean_dir()
    map = OnlyOne().map
    zfile,scale = params['mapzip'][map]
    plot_map.plot(pts, fout, zfile=zfile, scale=scale, pixel=True)
    return fout
    

@app.route('/trace')
def trace():
    df = pd.read_csv(params['gps'])
    pts = np.flip(np.array(df[['lat','lon']]), axis=0)
    OnlyOne().last_location = [pts[0][0],pts[0][1]]
    fout = plot_trace(pts)
    return render_template('/trace.html', location=fout)

@app.route('/city')
def city():
    return render_template('/city.html',data=OnlyOne().city_results)

@app.route('/place')
def place():
    return render_template('/place.html',data=OnlyOne().place_results)

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
                m = route.get_centroid(locs)
                locs = polyline.encode(locs,precision=6)
            else:                
                locs = eval(locs)
                m = locs
                locs = "%s;%s" % (locs[0],locs[1])

            lat2,lon2 = m
            d = geopy.distance.vincenty((lat2,lon2),(lat, lon))
            if d < 30.0:
                rowname = row[headers['Name']]
                rowdesc = row[headers['Description']]
                rowxx = [row[headers['CoordType']],
                         row[headers['Type']],
                         rowname,rowdesc,locs,np.round(d.km,2)]
                res.append(rowxx)
            
    OnlyOne().poi_results = res
    return poi()

@app.route('/poi_cache')
def poi_cache():
    lat,lon = my_curr_location()
    location = "%s,%s" % (lat,lon)
    tmppoi = os.environ['TMPDIR'] + "/poitmp.csv"
    fout = open (tmppoi,"w")
    for type in ['campground','atm','bus_station','shopping_mall','hospital','taxi_stand']:
        url = place_query2 % (location, type, params['api'])
        print (url)
        html = urlopen(url)
        json_res = json.loads(html.read().decode('utf-8'))
        for x in json_res['results']:
            olat = x['geometry']['location']['lat']
            olon = x['geometry']['location']['lng']
            line = "%s|%s|%s|%s|%s|[%s,%s]" % (type,"X",x['name'],"","Single",olat,olon)
            fout.write(line)
            fout.write("\n")
        fout.flush()
    fout.close()

    destination = open(params['poi'],'wb')
    shutil.copyfileobj(open(params['poi_base'],'rb'), destination)
    shutil.copyfileobj(open(tmppoi,'rb'), destination)
    destination.close()     
    return render_template('/results.html',data="done")

def get_elev(lat,lon):
    connmod = sqlite3.connect(params['elevdbmod'])
    cm = connmod.cursor()    
    elev = route.get_elev_single(lat,lon,cm)
    print ('elev',elev)
    return np.round(elev,2)

@app.route('/gogeos/<coords>/<refresh>')
def gogeos(coords, refresh):
    lat,lon = coords.split(';')
    lat2,lon2 = my_curr_location()
    bearing = route.get_bearing((lat2,lon2),(float(lat),float(lon)))
    distance = geopy.distance.vincenty((lat2,lon2),(lat, lon))
    distance = np.round(distance.km, 2)

    vin = os.environ['TMPDIR'] + "/locspeak.txt"
    vout = os.environ['TMPDIR'] + "/out.wav"
    fout = open(vin,"w")
    fout.write("You are %0.2f away. Keep walking towards %0.2f degrees" % (distance,bearing))
    fout.close()

    os.system("espeak -f %s -v en-us -w %s" % (vin,vout))
    os.system("mpv %s" % vout) 
    
    pts = np.array([[lat, lon],[lat2,lon2]]).astype(float)
    fout = "static/out-%s.png" % uuid.uuid4()
    clean_dir()
    OnlyOne().last_location = [lat,lon]
    map = OnlyOne().map
    zfile,scale = params['mapzip'][map]
    plot_map.plot(pts, fout, zfile=zfile, scale=scale)
    walking = been_walking()
    elev = get_elev(float(lat),float(lon))
    return render_template('/locations.html', location=fout, walking=walking, bearing=bearing, distance=distance, lat=lat, lon=lon, elev=elev, refresh=refresh)
    
@app.route('/gogeo/<coords>')
def gogeo(coords):
    lat,lon = coords.split(';')
    lat2,lon2 = my_curr_location()
    bearing = route.get_bearing((lat2,lon2),(float(lat),float(lon)))
    distance = geopy.distance.vincenty((lat2,lon2),(lat, lon))
    distance = np.round(distance.km, 2)
    pts = np.array([[lat, lon],[lat2,lon2]]).astype(float)
    fout = "static/out-%s.png" % uuid.uuid4()
    clean_dir()
    OnlyOne().last_location = [lat,lon]
    map = OnlyOne().map
    zfile,scale = params['mapzip'][map]
    plot_map.plot(pts, fout, zfile=zfile, scale=scale)
    walking = been_walking()
    elev = get_elev(float(lat),float(lon))
    return render_template('/location.html', location=fout, walking=walking, bearing=bearing, distance=distance, lat=lat, lon=lon, elev=elev)

@app.route('/reset/<what>')
def reset(what):
    msg = ""
    if what == "log":
        df = pd.read_csv(params['gps'])
        df = df.tail(1)
        msg = "Log is reset"
        df.to_csv(params['gps'],index=None)
    return render_template('/reset.html', msg=msg)

@app.route("/place_search", methods=["POST"])
def place_search():
    query = request.form.get("keyword").strip().replace(" ","+").lower()
    stype = request.form.get("type").lower()
    radius = int(request.form.get("radius"))
    lat,lon = my_curr_location()
    location = "%s,%s" % (lat,lon)    
    url = place_query % (location, radius, stype, query, params['api'])
    print (url)
    html = urlopen(url)
    json_res = json.loads(html.read().decode('utf-8'))
    res = []
    for x in json_res['results']:
        olat = x['geometry']['location']['lat']
        olon = x['geometry']['location']['lng']
        d = geopy.distance.vincenty((lat,lon),(olat,olon))
        res.append([x['name'],olat,olon,np.round(d.km,2)])
    OnlyOne().place_results = res
    return place()


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

    #lat,lon = my_curr_location()
    OnlyOne().last_location = [lat,lon]

    lat2,lon2 = my_curr_location()
    OnlyOne().last_gpx_file = gpx_file
    fout = plot_trail(lat, lon, gpx_file, (lat2,lon2))  

    first_point = None
    for track in gpx.tracks:
        for segment in track.segments:
            for i,point in enumerate(segment.points):
                if i==0: first_point = point
                if prev:
                    prev_dist = geopy.distance.vincenty((point.latitude, point.longitude),(prev.latitude,prev.longitude))
                    total_dist +=  prev_dist.km
                if point.elevation:
                    elevs = True
                    if point.elevation < elev_min: elev_min = point.elevation
                    if point.elevation > elev_max: elev_max = point.elevation
                d = geopy.distance.vincenty((point.latitude, point.longitude),(lat,lon))
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
           if prev: curr_dist += (geopy.distance.vincenty((dists[i][0].latitude,
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
    map = OnlyOne().map
    zfile,scale = params['mapzip'][map]
    plot_map.plot2(pts, fout, zfile=zfile, scale=scale, map_retrieval_on=(lat,lon), my_curr_location=my_curr_location, pixel=True)
    return fout

@app.route('/trails_nav_action', methods=['GET', 'POST'])
def trails_nav_action():
    res = step(request, OnlyOne().last_location, float(request.form['distance']))    
    OnlyOne().last_location = res
    fout = plot_trail(res[0], res[1], OnlyOne().last_gpx_file, res)
    print (fout)
    return render_template('/trail.html', location=fout)

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
                    d = geopy.distance.vincenty((lat2,lon2),(lat,lon)).km
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

    lat2,lon2 = my_curr_location()

    # sample / interpolate to create more points from line segments
    # so closest point is more fine grained
    roi = np.array(locs)
    steps = np.linspace(roi[:,0].min(), roi[:,0].max(),100.0)
    fsampled = np.interp(steps, roi[:,0], roi[:,1])
    fs2 = [[lat,lon] for (lat,lon) in zip(steps, fsampled)]
    df = pd.DataFrame(fs2)
    df.loc[:,'dist'] = df.apply(lambda x: geopy.distance.vincenty((lat2,lon2),(x[0],x[1])).km, axis=1)
    res = df.ix[df['dist'].idxmin()]
    c = [res[0],res[1]]
    d = geopy.distance.vincenty((lat2,lon2),c).km
    b = route.get_bearing([lat2,lon2],(c[0],c[1]))

    print (d,b)
    
    fout = "static/out-%s.png" % uuid.uuid4()
    clean_dir()
    map = OnlyOne().map
    zfile,scale = params['mapzip'][map]
    locs.insert(0,(lat2,lon2))
    locs.insert(0,c)        
    plot_map.plot(locs, fout, zfile=zfile, scale=scale, pixel=True, bp=True)
    return render_template('/poly.html', location=fout, distance=d, bearing=b)

@app.route('/lineelev')
def lineelev():
    return render_template('/lineelev.html',data=OnlyOne().line_elev_results)

@app.route("/line_elev_calc", methods=["POST"])
def line_elev_calc():
    npts = request.form.get("npts")
    bearing = int(request.form.get("bearing"))
    far = float(request.form.get("far")) / 1000.0
    lat,lon = my_curr_location()
    locs = []
    for x in np.linspace(0,far,npts):
        locs.append(tuple(route.goto_from_coord([lat,lon], x, bearing)))
    
    res = route.get_elev_data_ex_chunk(locs)

    plt.figure()
    plt.plot(np.linspace(0,far,npts),res)
    fout = "static/out-%s.png" % uuid.uuid4()
    plt.savefig(fout)
    return render_template('/lineelev.html', fout=fout)
    

@app.route('/gogoogelevline/<coords>')
def gogoogelevline(coords):
    npts = 200
    lat,lon = my_curr_location()
    lat2,lon2 = coords.split(';')
    lat2 = float(lat2)
    lon2 = float(lon2)
    far = geopy.distance.vincenty((lat,lon),(lat2,lon2)).km
    bearing = route.get_bearing((lat,lon),(lat2,lon2))
    locs = []
    for x in np.linspace(0,far,npts):
        locs.append(tuple(route.goto_from_coord([lat,lon], x, bearing)))
    res = route.get_elev_data_ex_chunk(locs)
    plt.figure()
    plt.plot(np.linspace(0,far,npts),res)
    clean_dir()
    fout = "static/out-%s.png" % uuid.uuid4()
    plt.savefig(fout)
    return render_template('/lineelev.html', fout=fout)


@app.route('/book/<init>')
def book(init):
    if init=='yes': OnlyOne().book_results = []
    return render_template('/book.html',base=params['book_base_url'],data=OnlyOne().book_results)

@app.route("/book_search", methods=["POST"])
def book_search():
    query = request.form.get("keyword").lower()
    res = loogle3.search(query, params['book_idx'])
    full_res = [str(x[0]) for x in res]
    OnlyOne().book_results = full_res
    return book(init='no')

@app.route('/celeb')
def celeb():
    return render_template('/celeb.html',data=OnlyOne().celeb_results)

@app.route("/celeb_search", methods=["POST"])
def celeb_search():
    keyword = request.form.get("keyword").lower()
    print (keyword)
    rd = csv.reader(codecs.open(params['celeb'],encoding="utf-8"),delimiter=':')
    headers = {k: v for v, k in enumerate(next(rd))}
    res = []
    for row in rd:
        if keyword in row[headers['Name']].lower() or keyword in row[headers['Description']].lower():
            d = datetime.datetime.strptime(row[headers['Birthday']], "%d/%m/%Y")
            d = d.strftime('%Y%m%d')
            res.append([ row[headers['Name']], d ])
            
    OnlyOne().celeb_results =res
    print (len(res))
    return celeb()

def download_song(song_url):
    ydl_opts = {
        'format': '140',
        'outtmpl': params['tube_dir'] + '/%(title)s.%(ext)s'
    }
    with youtube_dl.YoutubeDL(ydl_opts) as ydl:
        info_dict = ydl.extract_info(song_url, download=True) 

@app.route('/tube')
def tube():
    return render_template('/tube.html')


@app.route("/tube_dload", methods=["POST"])
def tube_dload():
    url = request.form.get("url")
    download_song(url)
    return tube()

if __name__ == '__main__':
    app.debug = True
    app.run(host="localhost",port=5000)
       
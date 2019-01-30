# -*- coding: utf-8 -*-
from flask import Flask, render_template, request
import numpy as np, pandas as pd, os, uuid, glob
import sys; sys.path.append("../map")
import sys; sys.path.append("../../guide")
import sys; sys.path.append("../..")
import plot_map, json, random, mindmeld
import geopy.distance, datetime, shutil
import news, csv, io, zipfile, math
from urllib.request import urlopen
import urllib, requests, json
from bs4 import BeautifulSoup
import gpxpy, gpxpy.gpx, polyline
from io import StringIO
import route

app = Flask(__name__)

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())
print (params)

place_query = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%s&radius=%d&type=%s&keyword=%s&key=%s"

elev_query = "https://maps.googleapis.com/maps/api/elevation/json?locations=enc:%s&key=%s"

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
            res['1000'] = route.get_bearing(float(df1.loc[idx,'lat']),
                                            float(df1.loc[idx,'lon']),
                                            mylat,
                                            mylon)
        if currd-200 <60:
            res['200'] = route.get_bearing(float(df1.loc[idx,'lat']),
                                           float(df1.loc[idx,'lon']),
                                           mylat,
                                           mylon)
        if currd-100 < 20:
            res['100'] = route.get_bearing(float(df1.loc[idx,'lat']),
                                           float(df1.loc[idx,'lon']),
                                           mylat,
                                           mylon)
        if currd-50 < 30:
            res['50'] = route.get_bearing(float(df1.loc[idx,'lat']),
                                          float(df1.loc[idx,'lon']),
                                          mylat,
                                          mylon)
        if currd-10 < 5:
            res['10'] = route.get_bearing(float(df1.loc[idx,'lat']),
                                          float(df1.loc[idx,'lon']),
                                          mylat,
                                          mylon)
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
            self.place_results = []
            self.line_elev_results = []
            self.hay_results = []
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

@app.route('/')
def index():
    loc = str(my_curr_location())
    return render_template('/index.html', loc=loc)

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
    return render_template('/location.html', location=fout, walking=walking, lat=lat, lon=lon)

def plot_parks(lat, lon):
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
    map = OnlyOne().map
    zfile,scale = params['mapzip'][map]
    plot_map.plot_area(pt, parks, fout, zfile=zfile, scale=scale)
    return fout
    
@app.route('/parks')
def parks():    
    lat,lon = my_curr_location()
    OnlyOne().last_location = [lat,lon]
    fout = plot_parks(lat, lon)
    return render_template('/parks.html', location=fout)

def plot_camps(lat, lon):
    pt = np.array([[lat, lon]]).astype(float)
    df = pd.read_csv(params['campsites'], sep=',')
    df2 = df[['Site Name','Latitude','Longitude']]
    pts = []
    names = []
    pts.append([lat,lon])
    for idx in df.index:
        camp = (df2.ix[idx].Latitude, df2.ix[idx].Longitude)
        dist = geopy.distance.vincenty(camp,(lat,lon))
        if dist.km < float(params['natpark_mindistance']):
            pts.append(list(camp))
            names.append(df2.ix[idx]['Site Name'] + " " + str(dist.km))
            
    clean_dir()
    fout = "static/out-%s.png" % uuid.uuid4()
    map = OnlyOne().map
    zfile,scale = params['mapzip'][map]
    plot_map.plot(pts, fout, zfile=zfile, scale=scale)
    return fout,names
    
@app.route('/camps')
def camps():    
    lat,lon = my_curr_location()
    OnlyOne().last_location = [lat,lon]
    fout,names = plot_camps(lat, lon)
    return render_template('/camps.html', location=fout, names=names)

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
    print (d)
    res =  mindmeld.calculate(d)
    res['date'] = d
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

@app.route('/test')
def test():    
    return render_template('/out.html')

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
    return render_template('/location.html', location=fout)


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

@app.route('/news')
def news_action():
    nfile = "./templates/news.html"
    files_day = -1
    todays_day = datetime.datetime.now().day
    if os.path.isfile(nfile):
       files_day = datetime.datetime.fromtimestamp(os.path.getctime(nfile)).day
    # get news file once each day. if news file exists for today, dont get it again
    if files_day != todays_day:        
        print ('getting file')
        news.getnews(nfile)
        shutil.copy(nfile, params['secondary_news_dir'])
    return render_template('/news.html')

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
    name = request.form.get("name")
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

@app.route('/timedgogeo/<coords>')
def timedgogeo(coords):
    lat,lon = coords.split(';')
    return render_template('/timedloc.html', lat=lat, lon=lon)
    
@app.route('/gogeo/<coords>')
def gogeo(coords):
    lat,lon = coords.split(';')
    lat2,lon2 = my_curr_location()
    bearing = route.get_bearing(lat2,lon2,float(lat),float(lon))
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
    
    return render_template('/location.html', location=fout, walking=walking, bearing=bearing, distance=distance, lat=lat, lon=lon)

@app.route('/reset', methods=['GET', 'POST'])
def reset():
    if request.form['action'] == 'Yes':
        print ("yes")
        df = pd.read_csv(params['gps'])
        df = df.tail(1)
        df.to_csv(params['gps'],index=None)
    elif request.form['action'] == 'No':
        print ("no") 
    return index()

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

@app.route('/weather')
def weather():

  base_url = 'http://api.openweathermap.org/data/2.5/weather?'

  lat,lon = my_curr_location()
  payload = { 'lat': str(lat), 'lon': str(lon), 'units': 'metric', 'APPID': params['weatherapi'] }

  r = requests.get(base_url, params=payload) 
  res = []
  for x in r.iter_lines():
      x = json.loads(x.decode())
      res.append(x['name'])
      res.append (x['main'])
      res.append (x['wind'])
      res.append (('clouds', x['clouds']))

  base_url = 'http://api.openweathermap.org/data/2.5/forecast?'

  payload = { 'lat': str(lat), 'lon': str(lon), 'units': 'metric', 'APPID': params['weatherapi']}

  r = requests.get(base_url, params=payload) 

  for x in r.iter_lines():
      x = json.loads(x.decode())
      for xx in x['list']:
          rain = xx.get('rain')
          res.append ((xx['dt_txt'],
                       xx['weather'][0]['description'],
                       rain,
                       xx))
          res.append ('---------------')

  return render_template('/weather.html', res=res)

@app.route('/trail/<gpx_file>')
def trail(gpx_file):
    lat,lon = my_curr_location()
    OnlyOne().last_location = [lat,lon]
    OnlyOne().last_gpx_file = gpx_file
    fout = plot_trail(lat, lon, gpx_file)

    gpx_file = open(params['trails'] + "/" + gpx_file)
    gpx = gpxpy.parse(gpx_file)

    #lat,lon = (36.831865, 28.310244)
    disp = []

    elev_min = 10000
    elev_max = -10000
    total_dist = 0.0
    dists = []
    prev = None
    for track in gpx.tracks:
        for segment in track.segments:
            for point in segment.points:
                if prev:
                    prev_dist = geopy.distance.vincenty((point.latitude, point.longitude),(prev.latitude,prev.longitude))
                    total_dist +=  prev_dist.km
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
    
    return render_template('/trail.html', location=fout, disp=disp, link=gpx.link)

def plot_trail(lat, lon, gpx_file):
    pts = []
    gpx_file = open(params['trails'] + "/" + gpx_file)
    gpx = gpxpy.parse(gpx_file)
    pts.append([lat,lon])
    for track in gpx.tracks:
        for segment in track.segments:
            for point in segment.points:
                pts.append([point.latitude, point.longitude])

    clean_dir()
    fout = "static/out-%s.png" % uuid.uuid4()
    map = OnlyOne().map
    zfile,scale = params['mapzip'][map]
    plot_map.plot(pts, fout, zfile=zfile, scale=scale, pixel=True)
    return fout

@app.route('/trails_nav_action', methods=['GET', 'POST'])
def trails_nav_action():
    res = step(request, OnlyOne().last_location, float(request.form['distance']))    
    OnlyOne().last_location = res
    fout = plot_trail(res[0], res[1], OnlyOne().last_gpx_file)
    print (fout)
    return render_template('/trail.html', location=fout)

@app.route('/trails')
def trails():
    res = []
    files = glob.glob(params['trails'] + "/*.gpx" )
    for x in files:
        res.append(x[x.rindex('/')+1:])
    
    return render_template('/trails.html', res=res)

@app.route('/inc')
def inc():
    return render_template('/inc.html',rnd=np.round(random.random(),3))

@app.route('/tst')
def tst():
    return render_template('/tst.html')

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
    
    locs = polyline.encode(locs)
    print ('end',locs[-1])

    url = elev_query % (locs, params['api'])
    html = urlopen(url)
    json_res = json.loads(html.read().decode('utf-8'))
    res = []
    for x in json_res['results']:
        res.append(x['elevation'])

    OnlyOne().line_elev_results = res
    
    return lineelev()

@app.route('/flattestroute/<coords>')
def flattestroute(coords):
    lat1,lon1 = coords.split(';')
    lat1 = float(lat1)
    lon1 = float(lon1)
    lat2,lon2 = my_curr_location()

    lat11,lon11,lat22,lon22 = route.expand_coords(lat1,lon1,lat2,lon2)
    print (lat11,lon11,lat22,lon22)
    xo,yo = route.get_grid(lat11,lon11,lat22,lon22,npts=20)
    #xo,yo = route.get_grid(lat1,lon1,lat2,lon2,npts=15)
    coords = []
    start_idx = None
    end_idx = None
    for i in range(xo.shape[0]):
        for j in range(xo.shape[1]):
            coords.append((xo[i,j],yo[i,j]))
            if np.abs(xo[i,j]-lat1)<route.eps and np.abs(yo[i,j]-lon1)<route.eps:
                start_idx = (i,j)
            if np.abs(xo[i,j]-lat2)<route.eps and np.abs(yo[i,j]-lon2)<route.eps:
                end_idx = (i,j)

    print ('s',start_idx)
    print ('e',end_idx)
         
    locs = polyline.encode(coords)
    params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())
    url = elev_query % (locs, params['api'])
    html = urlopen(url)
    json_res = json.loads(html.read().decode('utf-8'))

    print ('xo',xo.shape)
    print ('len json',len(json_res['results']))
   
    elev_mat = np.zeros(xo.shape)   
    tmp = []
    for i in range(xo.shape[0]*xo.shape[1]):
        tmp.append(json_res['results'][i]['elevation'])
    elev_mat = np.array(tmp).reshape(xo.shape)
   
    print ('elev start', elev_mat[(0,0)])
    print ('elev end', elev_mat[(15,15)])
   
    p = route.dijkstra(elev_mat, start_idx, end_idx)
    pts = [(xo[c],yo[c]) for c in p]
    elevs = [elev_mat[c] for c in p]
    lines = ""
    lines += route.gpxbegin   
    templ = '<trkpt lat="%f" lon="%f"> <ele>%f</ele></trkpt>\n'
    for c in p:
        lines += templ % (xo[c],yo[c],elev_mat[c])
    lines += route.gpxend
    gpxfile = "01_calc_path.gpx"
    fout = open(params['trails'] + "/" + gpxfile,"w")
    fout.write(lines)
    fout.close()
    return trail(gpxfile)
    
if __name__ == '__main__':
    app.debug = True
    app.run(host="localhost",port=5000)
    
    

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

app = Flask(__name__)

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())
print (params)

def get_bearing(lat1,lon1,lat2,lon2):
    dLon = lon2 - lon1;
    y = math.sin(dLon) * math.cos(lat2);
    x = math.cos(lat1)*math.sin(lat2) - math.sin(lat1)*math.cos(lat2)*math.cos(dLon);
    brng = np.rad2deg(math.atan2(y, x));
    if brng < 0: brng+= 360
    return np.round(brng,2)

class OnlyOne(object):
    class __OnlyOne:
        def __init__(self):
            self.edible = None
            self.last_location = None
            self.map = "normal"
            self.edible_results = []
            self.city_results = []
            self.place_results = []
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
    return render_template('/index.html')

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
    return render_template('/location.html', location=fout)

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
        res = plot_map.goto_from_coord(OnlyOne().last_location,
                                       distance,
                                       0)
        
    elif request.form['action'] == '↓':
        res = plot_map.goto_from_coord(OnlyOne().last_location,
                                       distance,
                                       180)
    elif request.form['action'] == '→':
        res = plot_map.goto_from_coord(OnlyOne().last_location,
                                       distance,
                                       90)
    elif request.form['action'] == '←':
        res = plot_map.goto_from_coord(OnlyOne().last_location,
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
    return render_template('/mapset.html', maps=params['mapzip'].keys(), map=OnlyOne().map)

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
        shutil.copy(nfile, params['news_output_folder_for_audio'])
    return render_template('/news.html')

def plot_trace(pts):
    fout = "static/out-%s.png" % uuid.uuid4()
    clean_dir()
    map = OnlyOne().map
    zfile,scale = params['mapzip'][map]
    plot_map.plot(pts, fout, zfile=zfile, scale=scale)
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

@app.route('/gogeo/<coords>')
def gogeo(coords):
    lat,lon = coords.split(';')
    lat2,lon2 = my_curr_location()
    bearing = get_bearing(lat2,lon2,float(lat),float(lon))
    distance = geopy.distance.vincenty((lat2,lon2),(lat, lon))
    distance = np.round(distance.km, 2)
    pts = np.array([[lat, lon],[lat2,lon2]]).astype(float)
    fout = "static/out-%s.png" % uuid.uuid4()
    clean_dir()
    OnlyOne().last_location = [lat,lon]
    map = OnlyOne().map
    zfile,scale = params['mapzip'][map]
    plot_map.plot(pts, fout, zfile=zfile, scale=scale) 
    return render_template('/location.html', location=fout, bearing=bearing, distance=distance)

@app.route('/manual_geo')
def manual_geo():
    return render_template('/manual.html')

@app.route("/get_manual_geo", methods=["POST"])
def get_manual_geo():
    lat = request.form.get("lat")
    lon = request.form.get("lon")
    lat2,lon2 = my_curr_location()
    bearing = get_bearing(lat2,lon2,float(lat),float(lon))
    distance = geopy.distance.vincenty((lat2,lon2),(lat, lon))
    distance = np.round(distance.km, 2)
    pts = np.array([[lat, lon],[lat2,lon2]]).astype(float)
    fout = "static/out-%s.png" % uuid.uuid4()
    clean_dir()
    OnlyOne().last_location = [lat,lon]
    map = OnlyOne().map
    zfile,scale = params['mapzip'][map]
    plot_map.plot(pts, fout, zfile=zfile, scale=scale) 
    return render_template('/location.html', location=fout, bearing=bearing, distance=distance)

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
    lat,lon = my_curr_location()
    location = "%s,%s" % (lat,lon)    
    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%s&radius=1500&type=%s&keyword=%s&key=%s" % (location, stype, query, params['api'])
    print (url)
    html = urlopen(url)
    json_res = json.loads(html.read().decode('utf-8'))
    res = []
    for x in json_res['results']:
        res.append([x['name'],x['geometry']['location']['lat'],x['geometry']['location']['lng']])
    OnlyOne().place_results = res
    return place()


if __name__ == '__main__':
    app.debug = True
    app.run(host="localhost", port=5000)
    

# -*- coding: utf-8 -*-
from flask import Flask, render_template, request, session, send_file
from io import StringIO, BytesIO
import matplotlib.pyplot as plt, pickle, os
import numpy as np, pandas as pd, os, uuid, glob
import sys; sys.path.append("../guide")
import json, random, mindmeld, base64, time as timelib
import simplegeomap as sm
import geopy.distance, datetime, shutil
import csv, io, zipfile, folium
from urllib.request import urlopen
import urllib, requests, re
import gpxpy, gpxpy.gpx
import urllib.request as urllib2

app = Flask(__name__)

params = json.loads(open("nomterr.conf").read())

travel_url = "http://localhost:5000/static/travel"
    
def clean_dir():
    files = glob.glob("static/out-*.png")
    for f in files: os.remove(f)

def my_curr_location():
    lat,lon = session['geo']
    return lat,lon

@app.route('/')
def index():
    return render_template('/index.html')

@app.route('/location/<loc>/<zoom>')
def location(loc,zoom):
    lat,lon = loc.split(';')
    lat,lon=float(lat),float(lon)
    session['geo'] = (lat,lon)
    fout = "static/out-%s.png" % uuid.uuid4()
    clean_dir()
    lat,lon=float(lat),float(lon)
    fig = plt.figure()
    zoom = float(zoom)
    sm.plot_countries(lat,lon,zoom,outcolor='lavenderblush')    
    sm.plot_water(lat,lon,zoom)
    plt.plot(lon,lat,'rd')
    plt.savefig(fout)    
    return render_template('/location.html', location=fout, lat=lat, lon=lon)

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
        
@app.route('/travel_maps/<coords>/<resolution>')
def travel_maps(coords,resolution):

    travel_url = request.host_url + "static/travel"
    
    resolution = int(resolution)    
    fout = "/tmp/trav-%s.html" % uuid.uuid4()    
    data = urllib2.urlopen(travel_url + "/index.json").read().decode('utf-8')
    params = json.loads(data)

    clat,clon = params['center']
    m = folium.Map(location=[clat, clon], zoom_start=10, tiles="Stamen Terrain")

    currlat,currlon = coords.split(';')
    lat,lon=float(currlat),float(currlon)
    folium.Marker([lat,lon], icon=folium.Icon(color="green")).add_to(m)
    
    for p in params['points']:
        lat,lon = params['points'][p]
        folium.Marker([lat,lon], popup=p, icon=folium.Icon(color="blue")).add_to(m)

    for p in params['restaurants']:
        lat,lon = params['restaurants'][p]
        folium.Marker([lat,lon], popup=str(p), icon=folium.Icon(color="orange")).add_to(m)

    for p in params['campgrounds']:
        lat,lon = params['campgrounds'][p]
        folium.CircleMarker([lat,lon], popup=str(p), icon=folium.Icon(color="green")).add_to(m)
        
    rints = range(resolution)
    for map in params['maps']:
        mapurl = travel_url + "/" + map
        print (mapurl)
        data = urllib2.urlopen(mapurl).read().decode('utf-8')
        gpx = gpxpy.parse(data)
        points = []
        for track in gpx.tracks:
            for segment in track.segments:
                for point in segment.points:
                    if random.choice(rints) != 0: continue
                    lat,lon = point.latitude, point.longitude
                    points.append([lat,lon])

        folium.PolyLine(points, color='red', weight=1.0, opacity=1).add_to(m)
         
    m.save(fout)    
    return send_file(fout)


@app.route('/travel_maps_smgeo/<coords>/<zoom>')
def travel_maps_smgeo(coords,zoom):

    travel_url = request.host_url + "static/travel"

    fout = "/tmp/trav-%s.html" % uuid.uuid4()    
    data = urllib2.urlopen(travel_url + "/index.json").read().decode('utf-8')
    params = json.loads(data)

    zoom = float(zoom)
    eps = 0.001
    fout = "static/out-%s.png" % uuid.uuid4()
    clean_dir()
    
    clat,clon = params['center']

    currlat,currlon = coords.split(';')
    lat,lon=float(currlat),float(currlon)
    plt.plot(lon,lat,'gd')
    sm.plot_countries(clat,clon,zoom,outcolor='lavenderblush')    
    sm.plot_water(lat,lon,zoom)

    labels = ""
    i = 1
    for p in params['points']:
        lat,lon = params['points'][p]
        plt.plot(lon,lat,'bx')
        plt.text(lon+eps,lat+eps,str(i))
        labels += "%d %s <br/>" % (i,p)
        i += 1

    for p in params['campgrounds']:
        lat,lon = params['campgrounds'][p]
        plt.plot(lon,lat,'go')
        plt.text(lon+eps,lat+eps,str(i))
        labels += "%d %s <br/>" % (i,p)
        i += 1

    rints = range(4)
    for map in params['maps']:
        mapurl = travel_url + "/" + map
        print (mapurl)
        data = urllib2.urlopen(mapurl).read().decode('utf-8')
        gpx = gpxpy.parse(data)
        points = []
        for track in gpx.tracks:
            for segment in track.segments:
                for point in segment.points:
                    if random.choice(rints) != 0: continue
                    lat,lon = point.latitude, point.longitude
                    points.append([lat,lon])
        points = np.array(points)
        plt.plot(points[:,1],points[:,0],'red',alpha=0.4)
        
    plt.savefig(fout)
    plt.clf()
    return render_template('/travel.html', location=fout, lat=lat, lon=lon, labels=labels)


@app.route('/plot_elev/<coords>/<zoom>')
def plot_elev(coords,zoom):

    fout = "/tmp/out-%s.html" % uuid.uuid4()    

    zoom = float(zoom)
    fout = "static/out-%s.png" % uuid.uuid4()
    clean_dir()

    currlat,currlon = coords.split(';')
    lat,lon=float(currlat),float(currlon)
    plt.plot(lon,lat,'gd')
    sm.plot_countries(lat,lon,zoom,outcolor='lavenderblush')    
    sm.plot_elevation(lat,lon,zoom)
        
    plt.savefig(fout)
    plt.clf()
    return render_template('/elev.html', location=fout, lat=lat, lon=lon)


def get_weather(lat,lon):
    base_url = 'http://api.openweathermap.org/data/2.5/weather?'
    weatherapi = open(".owm").read()
    payload = { 'lat': str(lat), 'lon': str(lon), 'units': 'metric', 'APPID': weatherapi }
    r = requests.get(base_url, params=payload) 
    res = []
    for x in r.iter_lines():
        x = json.loads(x.decode())
        res.append(x['name'])
        res.append (x['main'])
        res.append (x['wind'])
        res.append (('clouds', x['clouds']))

    base_url = 'http://api.openweathermap.org/data/2.5/forecast?'
    payload = { 'lat': str(lat), 'lon': str(lon), 'units': 'metric', 'APPID': weatherapi }
    r = requests.get(base_url, params=payload) 

    for x in r.iter_lines():
        x = json.loads(x.decode())
        for xx in x['list']:
            rain = xx.get('rain')
            res.append ((xx['dt_txt'],xx['weather'][0]['description'],xx['main']['temp'], "C",
                         'feels_like :',xx['main']['feels_like'], "C",
                         'humidity :',xx['main']['humidity'],
                         'wind: ', xx['wind']['speed'], xx['wind']['deg']
            ))
            res.append ('---------------')
    return res

@app.route('/goweather/<coords>')
def goweather(coords):
    lat,lon = coords.split(';')
    res = get_weather(lat,lon)
    return render_template('/weather.html', res=res)

@app.route('/extnews')
def extnews():
    import news
    content = news.getnews()
    from flask import Response
    def generate():
        yield content
    return Response(generate(), mimetype='text/html')

@app.route('/market')
def market():
    plt.figure()
    end = datetime.datetime.now()
    start=end-datetime.timedelta(days=90)
    start = int(timelib.mktime(start.timetuple()))
    end = int(timelib.mktime(end.timetuple()))

    url = "https://query1.finance.yahoo.com/v7/finance/download/^IXIC?period1=" + str(start) + "&period2=" + str(end) + "&interval=1d&events=history&includeAdjustedClose=true"
    r = urllib2.urlopen(url).read()
    file = BytesIO(r)
    df1 = pd.read_csv(file,index_col='Date',parse_dates=True)

    url = "https://query1.finance.yahoo.com/v7/finance/download/^RUT?period1=" + str(start) + "&period2=" + str(end) + "&interval=1d&events=history&includeAdjustedClose=true"
    r = urllib2.urlopen(url).read()
    file = BytesIO(r)
    df2 = pd.read_csv(file,index_col='Date',parse_dates=True)

    ax1 = df2['Adj Close'].plot(color='blue', grid=True, label='Nasdaq')
    ax2 = df1['Adj Close'].plot(color='red', grid=True, label='Russell',secondary_y=True)
    h1, l1 = ax1.get_legend_handles_labels()
    h2, l2 = ax2.get_legend_handles_labels()
    plt.legend(h1+h2, l1+l2, loc=2)
    fout = "/tmp/out-%s.png" % uuid.uuid4()
    plt.savefig(fout)
    return send_file(fout)

@app.route('/gopollution/<coords>')
def gopollution(coords):
    lat,lon = coords.split(';')
    weatherapi = open(".owm").read()
    url = 'http://api.openweathermap.org/data/2.5/air_pollution?'
    payload = { 'lat': str(lat), 'lon': str(lon), 'appid': weatherapi }
    r = requests.get(url, params=payload)
    tmp = [json.loads(x.decode()) for x in r.iter_lines()]
    res = []
    res.append(tmp[0]['list'][0]['main'])
    comp = tmp[0]['list'][0]['components']
    for xx in comp: res.append ((xx, comp[xx]))
    return render_template('/weather.html', res=res)











if __name__ == '__main__':
    app.debug = True
    app.secret_key = "aksdfkasf"
    print (len(sys.argv))
    if len(sys.argv) == 2 and sys.argv[1]=="pi":
        app.run(host="192.168.43.89",port=5000)
    if len(sys.argv) == 2 and sys.argv[1]=="acer":
        app.run(host="192.168.43.49",port=5000)        
    else: 
        app.run(host="localhost",port=5000)


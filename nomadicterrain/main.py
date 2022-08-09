# -*- coding: utf-8 -*-
from flask import Flask, render_template, request, session, send_file
from io import StringIO
import matplotlib.pyplot as plt, pickle, os
import numpy as np, pandas as pd, os, uuid, glob
import sys; sys.path.append("../guide")
import json, random, mindmeld, base64, simplegeomap as sm
import geopy.distance, datetime, shutil, util
import csv, io, zipfile, folium
from urllib.request import urlopen
import urllib, requests, re
import gpxpy, gpxpy.gpx, polyline, codecs
import urllib.request as urllib2

app = Flask(__name__)

params = json.loads(open("nomterr.conf").read())
    
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
    resolution = int(resolution)
    
    fout = "/tmp/trav-%s.html" % uuid.uuid4()
    url = "http://localhost:5000/static/travel/index.json"
    data = urllib2.urlopen(url).read().decode('utf-8')
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

    rints = range(resolution)
    for map in params['maps']:
        mapurl = params['map_base'] + "/" + map
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
    fout = "/tmp/trav-%s.html" % uuid.uuid4()
    url = "http://localhost:5000/static/travel/index.json"
    data = urllib2.urlopen(url).read().decode('utf-8')
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
    for i,p in enumerate(params['points']):
        lat,lon = params['points'][p]
        #folium.Marker([lat,lon], popup=p, icon=folium.Icon(color="blue")).add_to(m)
        plt.plot(lon,lat,'rx')
        plt.text(lon+eps,lat+eps,str(i))
        labels += "%d %s <br/>" % (i,p)

    plt.savefig(fout)
    plt.clf()
    return render_template('/travel.html', location=fout, lat=lat, lon=lon, labels=labels)





if __name__ == '__main__':
    app.debug = True
    app.secret_key = "aksdfkasf"
    app.run(host="localhost",port=5000)


import urllib.parse, requests, json
import csv, numpy as np, re, os
import pandas as pd, json, folium
from unidecode import unidecode

params = json.loads(open(os.environ['HOME'] + "/.nomterr.conf").read())
city =  params['osm_city']
bus = params['osm_dir'] + "/" + unidecode(city.lower()) + "bus.json"
metro = params['osm_dir'] + "/" + unidecode(city.lower()) + "metro.json"
base_url = "https://overpass-api.de/api/interpreter?data="

def get_bus():

    q = """
    [out:json];
    area[name="%s"]->.a;
    (
      nwr["route"="bus"](area.a);
    );
    out body;
    >;
    out skel;
    """ % city

    safe_string = urllib.parse.quote_plus(q)
    r = requests.get(base_url + safe_string)
    fout = open(bus,"w")
    fout.write(r.text)
    fout.close()

def get_metro():

    q = """
    [out:json];
    area[name="%s"]->.a;
    (
      nwr["route"="subway"](area.a);
    );
    out body;
    >;
    out skel;
    """ % city

    safe_string = urllib.parse.quote_plus(q)
    r = requests.get(base_url + safe_string)
    fout = open(metro,"w")
    fout.write(r.text)
    fout.close()

def plot_bus():
    d = json.loads(open(bus).read())
    lines = {}
    nodes = {}
    for e in d['elements']:
        if 'lat' in e:
            nodes[e['id']] = (e['lat'],e['lon'])
            
    for e in d['elements']:
        if e['type'] == 'relation':
            if 'name' not in e['tags']: continue           
            print (e['tags']['name'])
            line = [m['ref'] for m in e['members']]
            lines[e['tags']['name']] = line

    m = folium.Map(location=[41,29], tiles='Stamen Terrain', zoom_start=10)
    
    for k in lines:
        coords = [nodes[m] for m in lines[k] if m in nodes]
        if len(coords)>0:
            folium.PolyLine(locations=coords, color="blue",weight=1).add_to(m)
    
    m.save("/tmp/out.html")
    
def plot_metro():
    d = json.loads(open(metro).read())
    lines = {}
    nodes = {}
    for e in d['elements']:
        if 'lat' in e:
            nodes[e['id']] = (e['lat'],e['lon'])
            
    for e in d['elements']:
        if e['type'] == 'relation':
            if 'name' not in e['tags']: continue
            print (e['tags']['name'])
            line = [m['ref'] for m in e['members']]
            lines[e['tags']['name']] = line

    m = folium.Map(location=[41,29], tiles='Stamen Terrain', zoom_start=10)
    
    for k in lines:
        coords = [nodes[m] for m in lines[k] if m in nodes]
        if len(coords)>0:
            folium.PolyLine(locations=coords, color="red",weight=1).add_to(m)
    
    m.save("/tmp/out.html")
    
if __name__ == "__main__": 

    #get_bus()
    #get_metro()
    plot_bus()
    #plot_metro()

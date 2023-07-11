import urllib.parse, requests
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
    
if __name__ == "__main__": 

    #get_bus()
    get_metro()

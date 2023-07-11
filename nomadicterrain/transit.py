import urllib.parse, requests
import csv, numpy as np, re, os
import pandas as pd, json, folium

params = json.loads(open(os.environ['HOME'] + "/.nomterr.conf").read())
bus = params['osm_dir'] + "/istbus.json"
metro = params['osm_dir'] + "/istmetro.json"
base_url = "https://overpass-api.de/api/interpreter?data="

def get_bus():

    q = """
    [out:json];
    area[name="İstanbul"]->.a;
    (
      nwr["route"="bus"](area.a);
    );
    out body;
    >;
    out skel;
    """

    safe_string = urllib.parse.quote_plus(q)
    r = requests.get(base_url + safe_string)
    fout = open(bus,"w")
    fout.write(r.text)
    fout.close()

def get_metro():

    q = """
    [out:json];
    area[name="İstanbul"]->.a;
    (
      nwr["route"="subway"](area.a);
    );
    out body;
    >;
    out skel;
    """

    safe_string = urllib.parse.quote_plus(q)
    r = requests.get(base_url + safe_string)
    fout = open(metro,"w")
    fout.write(r.text)
    fout.close()
    
if __name__ == "__main__": 

    get_bus()
    #get_metro()

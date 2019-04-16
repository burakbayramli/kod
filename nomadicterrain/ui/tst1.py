import geopy.distance
import numpy as np
from urllib.request import urlopen
import os, json

place_query = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%s&radius=30000&keyword=&type=%s&key=%s"

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

lat,lon = 40.250457, 28.958647999999997
location = "%s,%s" % (lat,lon)
for type in ['campground','atm','bus_station','shopping_mall']:
    url = place_query % (location, type, params['api'])
    print (url)
    html = urlopen(url)
    json_res = json.loads(html.read().decode('utf-8'))
    for x in json_res['results']:
        olat = x['geometry']['location']['lat']
        olon = x['geometry']['location']['lng']
        line = "%s|%s|%s|%s|[%s,%s]" % (type,"X",x['name'],"Single",olat,olon)
        print (line)


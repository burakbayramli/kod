import geopy.distance
import numpy as np
from urllib.request import urlopen
import os, json

#place_query = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=40.250457,28.958647999999997&radius=1500&type=restaurant&key=AIzaSyCxQopw-CIBAdyUrhYk18LC_qurTjUWWlE"
place_query = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=40.250457,28.958647999999997&radius=30000&keyword=&type=campground&key=AIzaSyCxQopw-CIBAdyUrhYk18LC_qurTjUWWlE"

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

lat,lon = 40.250457, 28.958647999999997
location = "%s,%s" % (lat,lon)
url = place_query
url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=40.250457,28.958647999999997&radius=30500&type=campsite&keyword=&key=AIzaSyCxQopw-CIBAdyUrhYk18LC_qurTjUWWlE"
print (url)
html = urlopen(url)
json_res = json.loads(html.read().decode('utf-8'))
print (json_res)

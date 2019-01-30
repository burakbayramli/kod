from urllib.request import urlopen
import polyline, json, os

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

elev_query = "https://maps.googleapis.com/maps/api/elevation/json?locations=enc:%s&key=%s"

lat2,lon2 = 36.648548, 32.039130

locs = [[lat2,lon2]]
locs = polyline.encode(locs)
print ('end',locs[-1])

url = elev_query % (locs, params['api'])
html = urlopen(url)
json_res = json.loads(html.read().decode('utf-8'))
print (json_res)



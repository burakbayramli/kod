import json, os, numpy as np

SROWS=40000

S = 8 # RBF grid division

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

if os.path.isdir("/tmp"): os.environ['TMPDIR'] = "/tmp"

elev_cmd = "curl  -d '%s' -XPOST -H 'Content-Type: application/json' https://elevation.racemap.com/api > " + os.environ['TMPDIR'] + "/elevout.txt"

elev_query = "https://maps.googleapis.com/maps/api/elevation/json?locations=enc:%s&key=%s"

gps_coord_sample_file = 'gps_coord_sample.npy'

gpsidx = np.load(params['coordidx'])

gpxbegin = '''<?xml version="1.0" encoding="UTF-8"?>
<gpx creator="Wikiloc - https://www.wikiloc.com" version="1.1"
     xmlns="http://www.topografix.com/GPX/1/1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
<metadata><name>ddddd</name><author><name>ddddd</name>
<link href="https://www.wikiloc.com/wikiloc/user.do?id=1111676">
<text>dddddd</text></link></author><link href="https://www.wikiloc.com/hiking-trails/alanya-oba-kadipinari-cayi-yuruyusu-6911676">
<text>Test1</text></link><time>2014-05-23T08:45:39Z</time></metadata>
<trk>
<name>Test1</name><cmt></cmt><desc>
</desc>
<trkseg>
'''

gpxend = '''
</trkseg>
</trk>
</gpx>
'''
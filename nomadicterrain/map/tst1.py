import numpy as np, plot_map, json, os, re
import matplotlib.pyplot as plt, csv
import geopy.distance, math, route, polyline
params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

rd = csv.reader(open(params['poi']),delimiter='|')
headers = {k: v for v, k in enumerate(next(rd))}
res = []
for row in rd:
    print (row[headers['Name']].lower())
    if u'torkul' in row[headers['Name']].lower():
        print ('-----------')
        locs = row[headers['Coords']]
        print ('locs',locs)
        if "[[" in locs:
            locs = eval(locs)
            locs = polyline.encode(locs)
        else:                
            locs = eval(locs)
            locs = "%s;%s" % (locs[0],locs[1])
        print (locs)
        name = row[headers['Name']]
        name = name.replace("'","")
        name = name.replace("[","")
        name = name.replace("]","")
        desc = row[headers['Description']]
        desc = desc.replace("'","")
        desc = desc.replace("[","")
        desc = desc.replace("]","")
        xx = [row[headers['CoordType']],
              row[headers['Type']],
              name,
              desc,
              locs]
        print ('xx',xx)
        res.append(xx)

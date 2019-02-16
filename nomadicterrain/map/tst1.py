import numpy as np, plot_map, json, os
import matplotlib.pyplot as plt, csv
import geopy.distance, math, route

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())
#print (params)

name = "Test"
name = name.lower()
rd = csv.reader(open(params['poi']),delimiter='|')
headers = {k: v for v, k in enumerate(next(rd))}
res = []
for row in rd:
    if name in row[headers['Type']].lower() or \
       name in row[headers['Name']].lower() or \
       name in row[headers['Description']].lower():
        res.append(row)
        print (row)

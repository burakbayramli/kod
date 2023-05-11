import numpy as np, matplotlib.pyplot as plt, os
from scipy.spatial.distance import cdist
import csv, numpy as np, re, pickle
import sqlite3

os.remove('nodes2.db')
db = sqlite3.connect('nodes2.db')

import random

cursor = db.cursor()

cursor.execute('''CREATE TABLE osm_nodes(id INTEGER PRIMARY KEY, 
                  lat NUMERIC, lon NUMERIC, c1 INTEGER, c2 INTEGER)
                  ''')

db.commit()

cursor = db.cursor()

with open('nodes2.csv') as csvfile:
    rd = csv.reader(csvfile,delimiter=',')
    headers = {k: v for v, k in enumerate(next(rd))}
    for i,row in enumerate(rd):        
        id,lat,lon,c1,c2 = row[headers['id']],row[headers['lat']],row[headers['lon']],row[headers['c1']],row[headers['c2']]
        cursor.execute('''INSERT INTO osm_nodes(id, lat, lon, c1, c2)
                  VALUES(?,?,?,?,?)''', (id,lat,lon,c1,c2))
        if i % 100000 == 0:
            print (i)
            db.commit()

        
cursor = db.cursor()
cmd = "CREATE INDEX index1 ON osm_nodes(c1)"
cursor.execute(cmd)
cmd = "CREATE INDEX index2 ON osm_nodes(c2)"
cursor.execute(cmd)
db.commit()


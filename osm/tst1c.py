from diskdict import DiskDict
import csv, numpy as np, re, pickle
from collections import defaultdict

dd = defaultdict(dict)

with open('edges.csv') as csvfile:
    rd = csv.reader(csvfile,delimiter=',')
    headers = {k: v for v, k in enumerate(next(rd))}
    for i,row in enumerate(rd):        
        if i % 100000 == 0: print (i)
        if row[headers['foot']] == 'Allowed':
            dd[row[headers['source']]][row[headers['target']]] = row[headers['length']]
            dd[row[headers['target']]][row[headers['source']]] = row[headers['length']]
        #if i>10000: break

pickle.dump(dd, open('walkdict2.pkl', 'wb')) 

from diskdict import DiskDict
import csv, numpy as np, re

dd = DiskDict('walkdict')

with open('edges.csv') as csvfile:
    rd = csv.reader(csvfile,delimiter=',')
    headers = {k: v for v, k in enumerate(next(rd))}
    for i,row in enumerate(rd):        
        if i % 100000 == 0: print (i)
        if row[headers['foot']] == 'Allowed':
            dd[row[headers['source']]] = {}
            dd[row[headers['target']]] = {}
        #if i>10000: break

dd.close()
        

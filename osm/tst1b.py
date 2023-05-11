from diskdict import DiskDict
import csv, numpy as np, re

dd = DiskDict('walkdict')
        
with open('edges.csv') as csvfile:
    rd = csv.reader(csvfile,delimiter=',')
    headers = {k: v for v, k in enumerate(next(rd))}
    for i,row in enumerate(rd):        
        if i % 100000 == 0: print (i)
        if row[headers['foot']] == 'Allowed':
            
            tmp = dd[row[headers['source']]]
            tmp[row[headers['target']]] = row[headers['length']]
            dd[row[headers['source']]] = tmp

            tmp = dd[row[headers['target']]]
            tmp[row[headers['source']]] = row[headers['length']]
            dd[row[headers['target']]] = tmp
            
        #if i>10000: break

dd.close()

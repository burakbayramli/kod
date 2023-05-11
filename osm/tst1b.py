from diskdict import DiskDict
import csv, numpy as np, re

dd = DiskDict('walkdict')
        
with open('edges.csv') as csvfile:
    rd = csv.reader(csvfile,delimiter=',')
    headers = {k: v for v, k in enumerate(next(rd))}
    for i,row in enumerate(rd):        
        if i % 100000 == 0: print (i)
        if row[headers['foot']] == 'Allowed':
            dd[row[headers['source']]][row[headers['target']]] = row[headers['length']]
            dd[row[headers['target']]][row[headers['source']]] = row[headers['length']]
            if row[headers['source']] == '2377631845':
                print (row)
                print (dd[row[headers['source']]][row[headers['target']]])
        #if i>10000: break

dd.close()

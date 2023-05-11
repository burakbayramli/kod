from sqlitedict import SqliteDict
import csv, numpy as np, re, pickle
from collections import defaultdict

def phase1():

    dd = SqliteDict("walkdict.sqlite")
    with open('edges.csv') as csvfile:
        rd = csv.reader(csvfile,delimiter=',')
        headers = {k: v for v, k in enumerate(next(rd))}
        for i,row in enumerate(rd):        
            if i % 100000 == 0:
                print (i)
                dd.commit()
            if row[headers['foot']] == 'Allowed':
                dd[row[headers['source']]] = {}
                dd[row[headers['target']]] = {}
        dd.commit()

def phase2():
    
    dd = SqliteDict("walkdict.sqlite")
    with open('edges.csv') as csvfile:
        rd = csv.reader(csvfile,delimiter=',')
        headers = {k: v for v, k in enumerate(next(rd))}
        for i,row in enumerate(rd):        
            if i % 100000 == 0:
                print (i)
                dd.commit()
            if row[headers['foot']] == 'Allowed':
                dd[row[headers['source']]][row[headers['target']]] = row[headers['length']]
                dd[row[headers['target']]][row[headers['source']]] = row[headers['length']]

        dd.commit()

phase2()        

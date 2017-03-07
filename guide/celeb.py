from datetime import datetime
import pandas as pd, mindmeld

fin = open('/home/burak/Documents/out.txt')
for line in fin:
    try:
        toks = line.strip().split(":")
        dd =  datetime.strptime(toks[2], '%d/%m/%Y').date()
        ddd = dd.strftime('%Y%m%d')
        res =  mindmeld.calculate(ddd)['millman']
        if res[0] == 38 and res[1] == 11: print toks[0], toks[2]
    except: pass

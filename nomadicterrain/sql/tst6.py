import json, sqlite3, os
import numpy as np
import pandas as pd

SROWS = 40000

#connmain = sqlite3.connect('/media/burak/Seagate Backup Plus Drive/archive/data/campdata/elev.db')
connmain = sqlite3.connect('/home/burak/Downloads/elev.db')

longmin,latmin,longmax,latmax =  2.553,56.15,31.833,71.5
print (longmin,latmin,longmax,latmax)
count = 0
total = 0
for lonint in (range(int(longmin),int(longmax)+1)):
    for latint in (range(int(latmin),int(latmax)+1)):
        print (latint,lonint)

        sql = "SELECT count(*) FROM ELEVATION WHERE latint=%d and lonint=%d" % (latint,lonint)
        res = connmain.execute(sql)
        res = list(res)
        print(res)
        if res[0][0] == SROWS: count += 1
        
        sql = "SELECT avg(elevation) FROM ELEVATION WHERE latint=%d and lonint=%d" % (latint,lonint)
        res = connmain.execute(sql)
        res = list(res)
        print(res[0][0])
        total += 1
        if res[0][0]!=SROWS:        
            pass
        else:
            print ('skipping...')
            continue
        
print (count, total, count / total * 100.0, 'percent of data retrieved')

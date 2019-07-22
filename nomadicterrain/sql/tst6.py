import json, sqlite3, os
import numpy as np
import pandas as pd

SROWS = 40000

connmain = sqlite3.connect('/media/burak/Seagate Backup Plus Drive/archive/data/campdata/elev.db')

longmin,latmin,longmax,latmax =  2.553,56.15,31.833,71.5
print (longmin,latmin,longmax,latmax)

for lonint in (range(int(longmin),int(longmax)+1)):
    for latint in (range(int(latmin),int(latmax)+1)):
        print (latint,lonint)

        sql = "SELECT count(*) FROM ELEVATION WHERE latint=%d and lonint=%d" % (latint,lonint)
        res = connmain.execute(sql)
        res = list(res)
        print(res)
        
        sql = "SELECT avg(elevation) FROM ELEVATION WHERE latint=%d and lonint=%d" % (latint,lonint)
        res = connmain.execute(sql)
        res = list(res)
        print(res[0][0])
        
        if res[0][0]!=SROWS:        
            pass
        else:
            print ('skipping...')
            continue


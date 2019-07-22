import json, sqlite3, os
import numpy as np
import pandas as pd

SROWS = 40000

connmain = sqlite3.connect('/media/burak/Seagate Backup Plus Drive/archive/data/campdata/elev.db')
conn1 = sqlite3.connect('/home/burak/Downloads/elev2.db')
#conn1 = sqlite3.connect('/media/burak/Seagate Backup Plus Drive/archive/data/campdata/elev.db')

longmin,latmin,longmax,latmax =  2.553,56.15,31.833,71.5
print (longmin,latmin,longmax,latmax)
#norway
for lonint in (range(int(longmin),int(longmax)+1)):
    for latint in (range(int(latmin),int(latmax)+1)):
        print (latint,lonint)

        sql = "SELECT count(*) FROM ELEVATION WHERE latint=%d and lonint=%d" % (latint,lonint)
        res = conn1.execute(sql)
        res = list(res)
        print(res)
        if res[0][0]!=SROWS:   
            c1 = conn1.cursor()
            sql = "DELETE FROM ELEVATION WHERE latint=%d and lonint=%d" % (latint,lonint)
            c1.execute(sql)
            conn1.commit()
        else:
            print ('skipping...')
            continue

        sql = "SELECT avg(elevation) FROM ELEVATION WHERE latint=%d and lonint=%d" % (latint,lonint)
        res = connmain.execute(sql)
        res = list(res)
        print(res[0][0])
        if (res[0][0] < 0.0):
            print ('skipping sea...')
            continue

        sql1 = "SELECT latint,lonint,lat,lon,elevation FROM ELEVATION WHERE latint=%d and lonint=%d" % (latint,lonint)
        res = connmain.execute(sql1)
        for i,(latint,lonint,lat,lon,elevation) in enumerate(res):
            if elevation == None:
                print ('elevation empty')
                continue
            sql = "INSERT INTO ELEVATION(latint,lonint,lat,lon,elevation) VALUES(%d,%d,%f,%f,%f);" %(latint,lonint,lat,lon,elevation)
            res = conn1.execute(sql)
            if i%200==0:
                print (i)
                conn1.commit()

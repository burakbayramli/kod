import json, sqlite3, os
import numpy as np
import pandas as pd
SROWS = 40000

connmain = sqlite3.connect('./campdata/elev.db')
conn1 = sqlite3.connect('elev2.db')

def get_all_countries():
    #df = pd.read_csv('bb-country-2.csv')
    df = pd.read_csv('tmp2.csv')
    #print (df)
    for row in np.array(df):
        country,longmin,latmin,longmax,latmax =  (row[0],row[2],row[3],row[4],row[5])
        print (country)
        print ('=================================')
        print (longmin,latmin,longmax,latmax)
        for lonint in (range(int(longmin),int(longmax)+1)):
            for latint in (range(int(latmin),int(latmax)+1)):
                print (lonint,latint)

                sql = "SELECT count(*) FROM ELEVATION WHERE latint=%d and lonint=%d" % (latint,lonint)
                res = conn1.execute(sql)
                res = list(res)
                print(res)
                if res[0][0]!=SROWS:
                    print ('no rows here')
                    exit()
                else:
                    print ('skipping...')
                    continue
                
                sql1 = "SELECT latint,lonint,lat,lon,elevation FROM ELEVATION WHERE latint=%d and lonint=%d" % (latint,lonint)
                res = connmain.execute(sql1)
                for i,(latint,lonint,lat,lon,elevation) in enumerate(res):
                    sql = "INSERT INTO ELEVATION(latint,lonint,lat,lon,elevation) VALUES(%d,%d,%f,%f,%f);" %(latint,lonint,lat,lon,elevation)
                    res = conn1.execute(sql)
                    if i%200==0:
                        print (i)
                        conn1.commit()
                                    

get_all_countries()

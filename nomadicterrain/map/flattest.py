from scipy.interpolate import Rbf
import numpy as np, plot_map, json, os
import geopy.distance, math, route
from datetime import timedelta
import datetime, sqlite3, pickle

SROWS = 40000
params = json.loads(open(os.environ['HOME'] + "/Downloads/campdata/nomterr.conf").read())

def insert_rbf_recs(latint,lonint,conn,connmod):
    c = conn.cursor()    
    cm = connmod.cursor()    
    sql = "DELETE FROM ELEVRBF where latint=%d and lonint=%d" % (latint, lonint)
    cm.execute(sql)
    connmod.commit()
    for lati in [0,2,4,6,8]:
        for lonj in [0,2,4,6,8]:
            sql = "SELECT lat,lon,elevation FROM ELEVATION WHERE latint=%d and lonint=%d " % (latint,lonint)
            res = c.execute(sql)
            X = []; Z=[]
            for (lat,lon,elevation) in res:
                if (".%d"%lati in str(lat) or ".%d"%(lati+1) in str(lat)) and \
                   (".%d"%lonj in str(lon) or ".%d"%(lonj+1) in str(lon)): 
                    #print (lat,lon,elevation)
                    X.append([lon,lat])
                    Z.append([elevation])
    
            X = np.array(X)
            Z = np.array(Z)
            X = X[Z[:,0]>0.0]
            Z = Z[Z[:,0]>0.0]
            print (X.shape)
            if X.shape[0]!=0: 
                rbfi = Rbf(X[:,0], X[:,1], Z)
                wdf = pickle.dumps(rbfi)
                cm.execute("INSERT INTO ELEVRBF(latint,lonint,lati,lonj,W) VALUES(?,?,?,?,?);",(latint, lonint, lati, lonj, wdf))
                connmod.commit()

def do_all_rbf_ints():

    conn = sqlite3.connect(params['elevdb'])
    connmod = sqlite3.connect(params['elevdbmod'])

    c = conn.cursor()
    #c.execute("delete from RBF1")
    res = c.execute('''select distinct latint, lonint from elevation; ''')

    for (latint,lonint) in res:
        print ('int---->', latint,lonint)

        sql1 = "SELECT count(*) FROM ELEVATION WHERE latint=%d and lonint=%d; " % (latint,lonint)
        c2 = conn.cursor()
        res1 = c2.execute(sql1)
        res1 = list(res1)

        insert_rbf_recs(latint,lonint,conn,connmod)        
        break

    c.close()
    conn.close()
    connmod.close()


lat1,lon1 = 41.084967,31.126588
lat1 = float(lat1)
lon1 = float(lon1)
lat2,lon2 = 40.749752,31.610694
    
conn = sqlite3.connect(params['elevdb'])
connmod = sqlite3.connect(params['elevdbmod'])
#do_all_rbf_ints()    
insert_rbf_recs(40,31,conn,connmod)

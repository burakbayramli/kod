from scipy.interpolate import Rbf
import numpy as np, plot_map, json, os
import geopy.distance, math, route
from datetime import timedelta
import datetime, sqlite3, pickle, re

SROWS = 40000
params = json.loads(open(os.environ['HOME'] + "/Downloads/campdata/nomterr.conf").read())

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

def insert_rbf_recs(latint,lonint,conn,connmod):
    c = conn.cursor()    
    cm = connmod.cursor()    
    sql = "DELETE FROM ELEVRBF where latint=%d and lonint=%d" % (latint, lonint)
    cm.execute(sql)
    connmod.commit()
    for lati in range(10):
        for lonj in range(10):
            sql = "SELECT lat,lon,elevation FROM ELEVATION WHERE latint=%d and lonint=%d " % (latint,lonint)
            res = c.execute(sql)
            X = []; Z=[]
            for (lat,lon,elevation) in res:
                if (".%d"%lati in str(lat)) and \
                   (".%d"%lonj in str(lon)): 
                    X.append([lon,lat])
                    Z.append([elevation])
    
            X = np.array(X)
            Z = np.array(Z)
            X = X[Z[:,0]>0.0]
            Z = Z[Z[:,0]>0.0]
            print (X.shape)
            if X.shape[0]!=0: 
                rbfi = Rbf(X[:,0], X[:,1], Z,function='gaussian')
                wdf = pickle.dumps(rbfi)
                cm.execute("INSERT INTO ELEVRBF(latint,lonint,lati,lonj,W) VALUES(?,?,?,?,?);",(latint, lonint, lati, lonj, wdf))
                connmod.commit()


def get_elev_single(lat,lon,connmod):
    cm = connmod.cursor()
    latint,lonint = int(lat),int(lon)
    lati = re.findall("\.(\d)",str(lat))[0]
    lonj = re.findall("\.(\d)",str(lon))[0]
    lati = int(lati)
    lonj = int(lonj)
    sql = "SELECT W from ELEVRBF where latint=? and lonint=? and lati=? and lonj=? " 

    r = cm.execute(sql,(latint,lonint,lati,lonj))
    r = list(r)
    if len(r)==0: return None
    rbfi = r[0]
    rbfi = pickle.loads(rbfi[0])
    return rbfi(lon, lat)

def plot_topo(lat1,lon1,fout1,fout2,fout3,how_far):
    D = 30
    boxlat1,boxlon1 = route.goto_from_coord((lat1,lon1), how_far, 45)
    boxlat2,boxlon2 = route.goto_from_coord((lat1,lon1), how_far, 215)

    boxlatlow = np.min([boxlat1,boxlat2])
    boxlonlow = np.min([boxlon1,boxlon2])
    boxlathigh = np.max([boxlat1,boxlat2])
    boxlonhigh = np.max([boxlon1,boxlon2])

    x = np.linspace(boxlonlow,boxlonhigh,D)
    y = np.linspace(boxlatlow,boxlathigh,D)

    xx,yy = np.meshgrid(x,y)
    
    conn = sqlite3.connect(params['elevdbmod'])
    c = conn.cursor()

    d = {}
    for lat,lon in zip(xx.flatten(),yy.flatten()):
        latint = int(lat)
        lonint = int(lon)
        lati = re.findall("\.(\d)",str(lat))[0]
        lonj = re.findall("\.(\d)",str(lon))[0]
        d[latint,lati,lonint,lonj] = "-"

    print (d)

        

def test_single_rbf_block():
    conn = sqlite3.connect(params['elevdb'])
    connmod = sqlite3.connect(params['elevdbmod'])
    #do_all_rbf_ints()    
    insert_rbf_recs(40,31,conn,connmod)

    
def main_test():    
    lat1,lon1 = 41.084967,31.126588
    lat1 = float(lat1)
    lon1 = float(lon1)
    lat2,lon2 = 40.749752,31.610694
    lat3,lon3 = 40.776241, 31.579548
    connmod = sqlite3.connect(params['elevdbmod'])
    #print (get_elev_single(lat2,lon2,connmod))
    #print (get_elev_single(lat3,lon3,connmod))

    fout1 = '/tmp/out1.png'
    fout2 = '/tmp/out2.png'
    fout3 = '/tmp/out3.png'
    plot_topo(lat1,lon1,fout1,fout2,fout3,10.0)
    
#test_single_rbf_block()    
main_test()


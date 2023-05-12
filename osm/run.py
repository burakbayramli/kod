import csv, numpy as np, re, os, shutil, pickle, simplegeomap as sm
from pygeodesy.sphericalNvector import LatLon
import matplotlib.pyplot as plt
import pandas as pd
from scipy.spatial.distance import cdist
from diskdict import DiskDict
import sqlite3

#seychelles
fr=(-4.699287820423064, 55.49185927728346)
to=(-4.6364140603708925, 55.406975784999176)

dbfile = "nodes2.db"

dictdir = "walkdict"

def diskdict():

    if os.path.exists(dictdir): shutil.rmtree(dictdir)
    
    dd = DiskDict(dictdir)
    with open('edges.csv') as csvfile:
        rd = csv.reader(csvfile,delimiter=',')
        headers = {k: v for v, k in enumerate(next(rd))}
        for i,row in enumerate(rd):        
            if i % 1000 == 0: print (i)
            if row[headers['foot']] == 'Allowed':
                dd[row[headers['source']]] = {}
                dd[row[headers['target']]] = {}

    dd.close()

    dd = DiskDict(dictdir)

    with open('edges.csv') as csvfile:
        rd = csv.reader(csvfile,delimiter=',')
        headers = {k: v for v, k in enumerate(next(rd))}
        for i,row in enumerate(rd):        
            if i % 1000 == 0: print (i)
            if row[headers['foot']] == 'Allowed':

                tmp = dd[row[headers['source']]]
                tmp[row[headers['target']]] = row[headers['length']]
                dd[row[headers['source']]] = tmp

                tmp = dd[row[headers['target']]]
                tmp[row[headers['source']]] = row[headers['length']]
                dd[row[headers['target']]] = tmp

    dd.close()

def grid_assign_centers():
    
    res1 = LatLon(-4.807419070202981, 55.364345234773644)
    res2 = LatLon(-4.549969190633921, 55.566362543604434)

    lowlat = np.min([res1.lat,res2.lat])
    lowlon = np.min([res1.lon,res2.lon])
    hilat = np.max([res1.lat,res2.lat])
    hilon = np.max([res1.lon,res2.lon])

    x = np.linspace(lowlon,hilon,3)
    y = np.linspace(lowlat,hilat,4)

    xx,yy = np.meshgrid(x,y)
    mids = []
    for x,y in zip(xx.flatten(), yy.flatten()):
        mids.append([x,y])
       
    mids = np.array(mids)

    sm.plot_countries(fr[0],fr[1],zoom=0.05)
    for x,y in zip(xx.flatten(), yy.flatten()):
        plt.plot(x,y,'rd')
    plt.savefig('out.jpg',quality=50)
        
    pickle.dump(mids, open('centers.pkl', 'wb'))        

    fout = open ("nodes2.csv","w")
    fout.write("%s,%s,%s,%s,%s\n" % ('id','lat','lon','c1','c2'))

    with open('nodes.csv') as csvfile:
        rd = csv.reader(csvfile,delimiter=',')
        headers = {k: v for v, k in enumerate(next(rd))}
        for i,row in enumerate(rd):        
            id,lat,lon = row[headers['id']],row[headers['lat']],row[headers['lon']]
            ds = cdist(mids,np.array([[lon,lat]]))
            res = list(np.argsort(ds,axis=0).T[0][:2])
            fout.write("%s,%s,%s,%d,%d\n" % (id,lat[:8],lon[:8],res[0],res[1]))
            fout.flush()
            if i % 1000 == 0:
                print ('i',i)

def insert_sql():
    if os.path.exists(dbfile): os.remove(dbfile)
    
    db = sqlite3.connect(dbfile)
    cursor = db.cursor()

    cursor.execute('''CREATE TABLE osm_nodes(id INTEGER PRIMARY KEY, 
                      lat NUMERIC, lon NUMERIC, c1 INTEGER, c2 INTEGER)
                      ''')

    db.commit()

    cursor = db.cursor()

    with open('nodes2.csv') as csvfile:
        rd = csv.reader(csvfile,delimiter=',')
        headers = {k: v for v, k in enumerate(next(rd))}
        for i,row in enumerate(rd):        
            id,lat,lon,c1,c2 = row[headers['id']],row[headers['lat']],row[headers['lon']],row[headers['c1']],row[headers['c2']]
            cursor.execute('''INSERT INTO osm_nodes(id, lat, lon, c1, c2)
                      VALUES(?,?,?,?,?)''', (id,lat,lon,c1,c2))
            if i % 1000 == 0:
                print (i)
                db.commit()


    cursor = db.cursor()
    cmd = "CREATE INDEX index1 ON osm_nodes(c1)"
    cursor.execute(cmd)
    cmd = "CREATE INDEX index2 ON osm_nodes(c2)"
    cursor.execute(cmd)
    db.commit()

def find_closest_node(lat,lon):
    mids = pickle.load(open("centers.pkl","rb"))

    DB = 'nodes2.db'
    conn = sqlite3.connect(DB)

    frvec = np.array([lon,lat]).reshape(1,2)
    ds = cdist(mids,frvec)
    fr_closest_mid = list(np.argsort(ds,axis=0).T[0][:2])
    frres = []
    sql = "select id,lat,lon from osm_nodes where c1==? or c1==? or c2==? or c2==?"
    c = conn.cursor()
    rows = c.execute(sql,(int(fr_closest_mid[0]),
                          int(fr_closest_mid[1]),
                          int(fr_closest_mid[0]),
                          int(fr_closest_mid[1])))
    for row in rows: frres.append(row)

    df = pd.DataFrame(frres); df.columns = ['id','lat','lon']

    frres = cdist(df[['lon','lat']], frvec)
    res = df.iloc[np.argmin(frres)][['id','lat','lon']]
    print (list(res))

        
if __name__ == "__main__":
    
    #diskdict()
    #grid_assign_centers()
    #insert_sql()
    find_closest_node(fr[0],fr[1])
    

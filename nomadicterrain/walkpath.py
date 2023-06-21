"""
Create a database of walk only paths that are away from car roads
"""
import csv, numpy as np, re, os, shutil, pickle, gpxpy, gpxpy.gpx
import sqlite3, pandas as pd, json, util, folium
from pygeodesy.sphericalNvector import LatLon
from cachetools import FIFOCache
from cachetools import cached

gc1 = (35.02184257, 26.357822857)
gc2 = (42.50843234, 45.174331578)

wx = 30
wy = 20

params = json.loads(open(os.environ['HOME'] + "/.nomterr.conf").read())
dbfile = params['osm_dir'] + "/edges.db"
db = sqlite3.connect(dbfile)

def get_linestring(content):
   points = []
   c = content.replace("LINESTRING","").replace("(","").replace(")","")
   res = c.split(",")
   for x in res:
      cs = x.split()
      if len(cs)==2: points.append((float(cs[1]), float(cs[0])))
   return points

def grid_assign_centers(corner1,corner2):
    
    p1 = LatLon(corner1[0],corner1[1])
    p2 = LatLon(corner2[0],corner2[1])

    lowlat = np.min([p1.lat,p2.lat])
    lowlon = np.min([p1.lon,p2.lon])
    hilat = np.max([p1.lat,p2.lat])
    hilon = np.max([p1.lon,p2.lon])

    x = np.linspace(lowlon,hilon,wx)
    y = np.linspace(lowlat,hilat,wy)

    xx,yy = np.meshgrid(x,y)
    mids = []
    for x,y in zip(xx.flatten(), yy.flatten()):
        mids.append([x,y])       
    mids = np.array(mids)
    return mids

def create_table():
    if os.path.exists(dbfile): os.remove(dbfile)    
    db = sqlite3.connect(dbfile)
    cursor = db.cursor()
    cursor.execute('''CREATE TABLE osm_edges(id TEXT PRIMARY KEY, 
                      lat NUMERIC, lon NUMERIC, c1 INTEGER, c2 INTEGER,
                      car INTEGER, walk INTEGER, wkt TEXT)
                      ''')
    db.commit()   


def create_edges():
   from scipy.spatial.distance import cdist    
   create_table()

   gpts = grid_assign_centers(gc1,gc2)

   print (gpts)

   db = sqlite3.connect(dbfile)
   cursor = db.cursor()

   with open(params['osm_dir'] + '/edges.csv') as csvfile:
       rd = csv.reader(csvfile,delimiter=',')
       headers = {k: v for v, k in enumerate(next(rd))}
       for i,row in enumerate(rd):
          id = row[headers['id']]
          if i % 100000 == 0:
             print (i)
             db.commit()

          car = 0; walk = 0
          ps = get_linestring(row[headers['wkt']])

          if row[headers['foot']] != 'Forbidden': walk = 1

          if row[headers['car_forward']] != 'Forbidden' or \
             row[headers['car_backward']] != 'Forbidden': car = 1

          beglat,beglon = ps[0]
          endlat,endlon = ps[-1]
          lat = (beglat+endlat) / 2.
          lon = (beglon+endlon) / 2.
          ds = cdist(gpts,np.array([[lon,lat]]))
          res = list(np.argsort(ds,axis=0).T[0][:2])
          cursor.execute('''INSERT INTO osm_edges(id, lat, lon, c1, c2,car,walk,wkt)
                            VALUES(?,?,?,?,?,?,?,?)''', ( id,lat,lon,int(res[0]),int(res[1]),car,walk,row[headers['wkt']] ) )

       db.commit()

   cmd = "CREATE INDEX index1 ON osm_edges(c1)"
   cursor.execute(cmd)
   cmd = "CREATE INDEX index2 ON osm_edges(c2)"
   cursor.execute(cmd)
   db.commit()

@cached(cache=FIFOCache(maxsize=10000))
def get_car_in_regions(c1):
   sql2 = "select id,lat,lon from osm_edges where car==1 and (c1==? or c2==?)"
   db = sqlite3.connect(dbfile)
   cursor1 = db.cursor()
   nrows = cursor1.execute(sql2, (c1,c1) )
   ids = []; neighs = []
   for nrow in nrows:
      nid,nlat,nlon = nrow[0],nrow[1],nrow[2]
      ids.append(nid)
      neighs.append([nlat,nlon])
   return np.array(ids),np.array(neighs)
      
def plot_walkable_paths(lat,lon):
   m = folium.Map(location=[gc1[0],gc1[1]], zoom_start=5)
   gpts = grid_assign_centers(gc1,gc2)   
   ds = util.cdist(np.array([[lon,lat]]),gpts)
   rid = str(np.argmin(ds))
   sql1 = "select id,lat,lon,c1,wkt from osm_edges where c1==? and walk==1"
   db = sqlite3.connect(dbfile)
   cursor1 = db.cursor()
   rows = cursor1.execute(sql1, (rid,) )
   gpx = gpxpy.gpx.GPX()
   for i,row in enumerate(rows):
      gpx_track = gpxpy.gpx.GPXTrack()   
      if i % 1000 == 0: print (i)
      id,lat,lon,c1,wkt = row[0],row[1],row[2],row[3],row[4]
      ids,neighs = get_car_in_regions(c1)
      neighs = neighs[ids != id]
      ds = util.cdist(np.array([[lat,lon]]),neighs)
      cllat,cllon = neighs[np.argmin(ds),:]
      p1 = LatLon(lat, lon)
      p2 = LatLon(cllat, cllon)
      d = p1.distanceTo(p2) / 1000.         
      if d > 1.0:
         gpx.tracks.append(gpx_track)
         gpx_segment = gpxpy.gpx.GPXTrackSegment()
         gpx_track.segments.append(gpx_segment)         
         ps = get_linestring(wkt)
         for p in ps: gpx_segment.points.append(gpxpy.gpx.GPXTrackPoint(p[0], p[1], elevation=0))
         folium.PolyLine(ps, color='blue', weight=2.0).add_to(m)
          
   m.save(params['osm_dir'] + '/hiking.html')
   fout = open(params['osm_dir'] + "/hiking.gpx","w")
   fout.write(gpx.to_xml())
   fout.close()
   
if __name__ == "__main__":
   
    #create_edges()
    plot_walkable_paths(38.304091991845006, 26.56341802127089)

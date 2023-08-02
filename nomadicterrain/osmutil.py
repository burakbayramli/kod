import urllib, requests, re, os, csv, shutil, util
import csv, numpy as np, re, os, shutil, pickle, sqlite3
from pygeodesy.sphericalNvector import LatLon
from priodict import priorityDictionary
import pandas as pd, json, folium
from sqlitedict import SqliteDict
from urllib.request import urlopen
import geopy.distance

params = json.loads(open(os.environ['HOME'] + "/.nomterr.conf").read())
dbfile = params['osm_dir'] + "/nodes.db"
edgedict = params['osm_dir'] + "/walkdict.sqlite"

def grid_assign_centers(corner1,corner2):
    
    p1 = LatLon(corner1[0],corner1[1])
    p2 = LatLon(corner2[0],corner2[1])

    lowlat = np.min([p1.lat,p2.lat])
    lowlon = np.min([p1.lon,p2.lon])
    hilat = np.max([p1.lat,p2.lat])
    hilon = np.max([p1.lon,p2.lon])

    # seychelles 3,4
    # tr 7,4
    x = np.linspace(lowlon,hilon,7)
    y = np.linspace(lowlat,hilat,4)

    xx,yy = np.meshgrid(x,y)
    mids = []
    for x,y in zip(xx.flatten(), yy.flatten()):
        mids.append([x,y])       
    mids = np.array(mids)
        
    pickle.dump(mids, open(params['osm_dir'] + '/centers.pkl', 'wb'))        

    if os.path.exists(dbfile): os.remove(dbfile)    
    db = sqlite3.connect(dbfile)
    cursor = db.cursor()
    cursor.execute('''CREATE TABLE osm_nodes(id INTEGER PRIMARY KEY, 
                      lat NUMERIC, lon NUMERIC, c1 INTEGER, c2 INTEGER)
                      ''')
    db.commit()
    
    cursor = db.cursor()
    with open(params['osm_dir'] + '/nodes.csv') as csvfile:
        rd = csv.reader(csvfile,delimiter=',')
        headers = {k: v for v, k in enumerate(next(rd))}
        for i,row in enumerate(rd):        
            id,lat,lon = row[headers['id']],row[headers['lat']],row[headers['lon']]
            ds = util.cdist(mids,np.array([[lon,lat]]))
            res = list(np.argsort(ds,axis=0).T[0][:2])
            cursor.execute('''INSERT INTO osm_nodes(id, lat, lon, c1, c2)
                      VALUES(?,?,?,?,?)''', (id,lat[:8],lon[:8],int(res[0]),int(res[1])))            
            if i % 100000 == 0:
                print (i)
                db.commit()

    cursor = db.cursor()
    cmd = "CREATE INDEX index1 ON osm_nodes(c1)"
    cursor.execute(cmd)
    cmd = "CREATE INDEX index2 ON osm_nodes(c2)"
    cursor.execute(cmd)
    db.commit()

def diskdict():

    if os.path.exists(dictdir): shutil.rmtree(dictdir)
    dd = DiskDict(dictdir)
    with open(params['osm_dir'] + '/edges.csv') as csvfile:
        rd = csv.reader(csvfile,delimiter=',')
        headers = {k: v for v, k in enumerate(next(rd))}
        for i,row in enumerate(rd):        
            if i % 100000 == 0: print (i)
            if row[headers['foot']] == 'Allowed':
                dd[row[headers['source']]] = {}
                dd[row[headers['target']]] = {}

    dd.close()

    dd = DiskDict(dictdir)
    with open(params['osm_dir'] + '/edges.csv') as csvfile:
        rd = csv.reader(csvfile,delimiter=',')
        headers = {k: v for v, k in enumerate(next(rd))}
        for i,row in enumerate(rd):        
            if i % 100000 == 0: print (i)
            if row[headers['foot']] == 'Allowed':

                tmp = dd[row[headers['source']]]
                tmp[row[headers['target']]] = row[headers['length']]
                dd[row[headers['source']]] = tmp

                tmp = dd[row[headers['target']]]
                tmp[row[headers['source']]] = row[headers['length']]
                dd[row[headers['target']]] = tmp

    dd.close()
    
def find_closest_node(lat,lon):
    mids = pickle.load(open(params['osm_dir'] + "/centers.pkl","rb"))

    conn = sqlite3.connect(dbfile)

    frvec = np.array([lon,lat]).reshape(1,2)
    ds = util.cdist(mids,frvec)
    fr_closest_mid = list(np.argsort(ds,axis=0).T[:2])
    frres = []
    sql = "select id,lat,lon from osm_nodes where c1==? or c1==? or c2==? or c2==?"
    c = conn.cursor()
    rows = c.execute(sql,(int(fr_closest_mid[0]),
                          int(fr_closest_mid[1]),
                          int(fr_closest_mid[0]),
                          int(fr_closest_mid[1])))
    for row in rows: frres.append(row)

    df = pd.DataFrame(frres); df.columns = ['id','lat','lon']

    frres = util.cdist(df[['lon','lat']], frvec)
    res = df.iloc[np.argmin(frres)][['id','lat','lon']]
    return list(res)

def Dijkstra(G, start, end=None):
    D = {}  
    P = {}  
    Q = priorityDictionary()  
    Q[start] = 0

    for v in Q:
        D[v] = Q[v]
        if v == end:
            break

        for w in G[v]:
            vwLength = D[v] + float(G[v][w])
            if w in D:
                if vwLength < D[w]:
                    raise ValueError("Dijkstra: found better path to already-final vertex")
            elif w not in Q or vwLength < Q[w]:
                Q[w] = vwLength
                P[w] = v

    return (D, P)

def shortest_path_nodes(G, start, end):

    D, P = Dijkstra(G, start, end)
    Path = []
    while 1:
        Path.append(end)
        if end == start:
            break
        end = P[end]
    Path.reverse()
    return Path

def get_osm_info(osmid):
    conn = sqlite3.connect(dbfile)
    sql = "select lat,lon from osm_nodes where id==?"
    c = conn.cursor()
    rows = list(c.execute(sql,(osmid,)))
    if (len(rows)==1): return rows[0]
    else: return None

def shortest_path_coords(fr, to):
    n1 = find_closest_node(fr[0],fr[1])
    n2 = find_closest_node(to[0],to[1])
    #dd = DiskDict(dictdir)
    dd = SqliteDict(edgedict)    
    path = shortest_path_nodes(dd,str(int(n1[0])),str(int(n2[0])))
    coords = [get_osm_info(x) for x in path]
    return coords

def get_amenities(amenity_type,amenity_name,amenity_dist,clat,clon):
    base_url = "https://overpass-api.de/api/interpreter?data="
    q = """
    [out:json];
    node["amenity"~"%s"](around:%s,%s,%s);
    out center;
    """ % (amenity_type,amenity_dist,clat,clon)
    safe_string = urllib.parse.quote_plus(q)
    r = requests.get(base_url + safe_string)    
    return json.loads(r.text)

def goto_from_coord(start, distance, bearing):
    """
    distance: in kilometers
    bearing: 0 degree is north, 90 is east
    """
    s = geopy.Point(start[0],start[1])
    d = geopy.distance.distance(kilometers = distance)
    return d.destination(point=s, bearing=bearing)

def get_camp(clat,clon,dist):
    base_url = "https://overpass-api.de/api/interpreter?data="
    res1 = goto_from_coord((clat,clon),dist/1000,45)
    res2 = goto_from_coord((clat,clon),dist/1000,225)
    lowlat = np.min([res1[0],res2[0]])
    lowlon = np.min([res1[1],res2[1]])
    hilat = np.max([res1[0],res2[0]])
    hilon = np.max([res1[1],res2[1]])
    q = """
    [out:json];
       node["tourism"="camp_site"]
       (%f,%f,%f,%f); out; (._;>;);
    out;
    """ % (lowlat,lowlon,hilat,hilon)
    safe_string = urllib.parse.quote_plus(q)
    r = requests.get(base_url + safe_string)    
    return json.loads(r.text)

def test1():
    #grid_assign_centers((36.52259447316748, 27.612981046240638),
    #                     (41.05628025861666, 42.58542464923075))   
    #diskdict()    
    fr=(41.01437162347757,29.164254494113184)
    to=(41.0497882628352,29.2460494538482)
    
    coords = shortest_path_coords(fr, to)

    m = folium.Map(location=fr, zoom_start=12)
    folium.PolyLine(locations=coords, color="red").add_to(m)
    m.save("/tmp/out.html")

def test2():
    get_camp(40.9671978651242, 29.0835162031361,30000)
        
if __name__ == "__main__": 
 
    test2()

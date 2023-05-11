import csv, numpy as np, re, pickle, pandas as pd
from scipy.spatial.distance import cdist
import sqlite3

mids = pickle.load(open("tr_centers.pkl","rb"))

DB = 'nodes2.db'
conn = sqlite3.connect(DB)

fr=(41.01437162347757,29.164254494113184)
to=(41.0497882628352,29.2460494538482)

frvec = np.array([fr[1],fr[0]]).reshape(1,2)
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
# 2377631845,41.01426,29.16417

tovec = np.array([to[1],to[0]]).reshape(1,2)
ds = cdist(mids,tovec)
to_closest_mid = list(np.argsort(ds,axis=0).T[0][:2])
tores = []
sql = "select id,lat,lon from osm_nodes where c1==? or c1==? or c2==? or c2==?"
c = conn.cursor()
rows = c.execute(sql,(int(to_closest_mid[0]),
                      int(to_closest_mid[1]),
                      int(to_closest_mid[0]),
                      int(to_closest_mid[1])))
for row in rows: tores.append(row)
conn.close()

df = pd.DataFrame(tores); df.columns = ['id','lat','lon']

tores = cdist(df[['lon','lat']], tovec)
res = df.iloc[np.argmin(tores)][['id','lat','lon']]
print (list(res))
#1364308852,41.04970,29.24614

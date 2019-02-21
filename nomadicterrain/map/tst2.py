from scipy.spatial.distance import cdist
import numpy.linalg as lin
import geopy.distance, sqlite3
from urllib.request import urlopen
import numpy as np, polyline, json
import os, pickle, math
import numpy as np, pandas as pd
from pqdict import pqdict

gamma = 0.3

SROWS=40000

S = 8 # RBF grid division

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())
        
def insert_rbf1_recs(latint,lonint):
    df=pd.DataFrame(np.linspace(0,1.0,S))
    df['s'] = df.shift(-1)
    print (df)
    
    conn = sqlite3.connect(params['elevdb'])
    c = conn.cursor()
    sql = "DELETE FROM RBF1 where latint=%d and lonint=%d" % (latint, lonint)
    c.execute(sql)
    conn.commit()
    sql = "SELECT lat,lon,elevation FROM ELEVATION WHERE latint=%d and lonint=%d " % (latint,lonint)
    res = list(c.execute(sql))    
    for i,r1 in enumerate(np.array(df)):
        for j,r2 in enumerate(np.array(df)):
            if j==S-1 or i==S-1: continue            
            X = []; Z=[]
            latlow = float(latint)+r1[0]
            lathigh = float(latint)+r1[1]
            lonlow = float(lonint)+r2[0]
            lonhigh = float(lonint)+r2[1]
            print (latlow,lathigh,lonlow,lonhigh)
            for (rlat,rlon,relev) in res:
                if rlat>=latlow and rlat<lathigh and rlon>=lonlow and rlon<lonhigh:
                    X.append([rlon,rlat])
                    Z.append([relev])
            print ('len',len(X))
            X = np.array(X)
            Z = np.array(Z)
            X = X[Z[:,0]>0.0]
            Z = Z[Z[:,0]>0.0]
            if (len(Z)<10): continue

            rbfi = Rbf(Z, Y, Z) 
                        
            c.execute("INSERT INTO RBF1(latint,lonint,latlow,lathigh,lonlow,lonhigh,gamma,W) VALUES(?,?,?,?,?,?,?,?);",(latint, lonint, latlow, lathigh, lonlow, lonhigh, gamma, rbfi))
            conn.commit()
    
def get_elev_single(lat,lon,c):
    sql = "SELECT W, gamma from RBF1 where ?>=latlow and ?<lathigh and ?>=lonlow and ?<lonhigh "
    r = c.execute(sql,(lat,lat,lon,lon))
    r = list(r)
    if len(r)==0: return -10.0
    W,gamma = r[0]
    df = pickle.loads(W)
    xr=np.array(df[0])
    xr=xr.reshape(len(xr),1)
    yr=np.array(df[1])
    yr=yr.reshape(len(xr),1)
    X = np.hstack((xr,yr))
    xnew = np.array([[lon,lat]])
    return np.multiply(df.w.T,np.exp(-gamma*lin.norm(X-xnew,axis=1))).sum()

def get_elev_data_grid_rbf(lat1,lon1,lat2,lon2,c,npts):
    xo,yo = get_grid(lat1,lon1,lat2,lon2,npts=npts)
    start_idx = None
    end_idx = None

    for eps in [0.003, 0.01, 0.1, 1.0]:
        for i in range(xo.shape[0]):
            for j in range(xo.shape[1]):
                if np.abs(xo[i,j]-lat1)<eps and np.abs(yo[i,j]-lon1)<eps:
                    start_idx = (i,j)
                if np.abs(xo[i,j]-lat2)<eps and np.abs(yo[i,j]-lon2)<eps:
                    end_idx = (i,j)
        if start_idx!=None and end_idx != None: break
         
    print ('s',start_idx)
    print ('e',end_idx)

    elev_mat = np.zeros(xo.shape)   
    for i in range(xo.shape[0]):
        for j in range(xo.shape[1]):
            elev_mat[i,j]=get_elev_single(xo[i,j],yo[i,j],c)
    
    return elev_mat, start_idx, end_idx, xo, yo 

def get_elev_data(latint, lonint):
    conn = sqlite3.connect(params['elevdb'])
    c = conn.cursor()
    sql = "SELECT count(*) FROM ELEVATION WHERE latint=%d and lonint=%d" % (latint,lonint)
    res = c.execute(sql)
    res = list(res)
    print(res)
    if res[0][0]<SROWS:
        print ('inserting')
        insert_gps_int_rows(latint,lonint)
    get_elev_goog(latint,lonint)
    insert_rbf1_recs(latint,lonint)

if __name__ == "__main__":
    #show_ints()
    #get_elev_data(42,19)
    insert_rbf1_recs(36,30)

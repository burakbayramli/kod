from mpl_toolkits.mplot3d import Axes3D
from scipy.spatial.distance import cdist
from matplotlib.colors import LightSource
from matplotlib import cm
import os, glob, re, zipfile
import pandas as pd, pickle
import numpy as np, sqlite3, json
import matplotlib.pyplot as plt
from PIL import Image
import geopy.distance, route
import pandas as pd, io

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

def plot2(points,outfile,zfile,scale,map_retrieval_on,my_curr_location,pixel=False,bp=True):
    plt.figure()
    center_res = map_retrieval_on
    imgcoord = []
    with zipfile.ZipFile(zfile, 'r') as z:
        for f in z.namelist():
            # the lat/lon middle of the map is encoded in the map's
            # filename
            tmp = re.findall("map_(\d+)_(\d+)_(\d+)_(\d+)",f,re.DOTALL)
            if len(tmp)==0: continue
            tmp = tmp[0]
            imgcoord.append([float(tmp[0] + "." + tmp[1]), float(tmp[2] + "." + tmp[3]), f])
    imgcoord2 = pd.DataFrame(imgcoord,columns=['lat','lon','file'])
    dists = imgcoord2.apply(lambda x: geopy.distance.vincenty((x['lat'],x['lon']),center_res).km, axis=1)
    # the closest map is picked
    found = imgcoord2.ix[dists.idxmin()]
    print (found.file)
    mapcenter = np.array(found[['lat','lon']])
    print (mapcenter)
    
    with zipfile.ZipFile(zfile, 'r') as z:
         im = Image.open(z.open(found.file))
         nim = np.array(im)
         c = nim.shape[0] / 2, nim.shape[0] / 2
         plt.axis('off')
         fig=plt.imshow(im)
         fig.axes.get_xaxis().set_visible(False)
         fig.axes.get_yaxis().set_visible(False)
         plt.imshow(im)

         lat,lon = map_retrieval_on
         dx,dy=((lon-mapcenter[1])*scale[0],(lat-mapcenter[0])*scale[1])
         xx = c[0]+dx
         yy = c[1]+dy
         if not (xx > nim.shape[0] or yy > nim.shape[1] or xx<0 or yy<0): 
             plt.plot(xx,yy,'ro',markersize=7,markerfacecolor='None')
         
         lat,lon = my_curr_location
         dx,dy=((lon-mapcenter[1])*scale[0],(lat-mapcenter[0])*scale[1])
         xx = c[0]+dx
         yy = c[1]+dy
         if not (xx > nim.shape[0] or yy > nim.shape[1] or xx<0 or yy<0): 
             plt.plot(xx,yy,'rx')
         
         for i,[lat,lon] in enumerate(points):
             dx,dy=((lon-mapcenter[1])*scale[0],(lat-mapcenter[0])*scale[1])
             xx = c[0]+dx
             yy = c[1]+dy
             if xx > nim.shape[0] or yy > nim.shape[1] or xx<0 or yy<0: continue
             if pixel:
                 plt.plot(xx,yy,'r,')
             else:
                 plt.plot(xx,yy,'r.')
         plt.savefig(outfile, bbox_inches='tight', pad_inches = 0, dpi = 300)



def plot(points,outfile,zfile,scale,pixel=False,bp=True):
    """
    Birinci noktayi baz alarak gerekli harita inajini bul, ve diger
    tum noktalari bu harita uzerinde grafikle
    """
    plt.figure()
    center_res = points[0]
    imgcoord = []
    with zipfile.ZipFile(zfile, 'r') as z:
        for f in z.namelist():
            # the lat/lon middle of the map is encoded in the map's
            # filename
            tmp = re.findall("map_(\d+)_(\d+)_(\d+)_(\d+)",f,re.DOTALL)
            if len(tmp)==0: continue
            tmp = tmp[0]
            imgcoord.append([float(tmp[0] + "." + tmp[1]), float(tmp[2] + "." + tmp[3]), f])
    imgcoord2 = pd.DataFrame(imgcoord,columns=['lat','lon','file'])
    dists = imgcoord2.apply(lambda x: geopy.distance.vincenty((x['lat'],x['lon']),center_res).km, axis=1)
    # the closest map is picked
    found = imgcoord2.ix[dists.idxmin()]
    print (found.file)
    mapcenter = np.array(found[['lat','lon']])
    print (mapcenter)
    
    with zipfile.ZipFile(zfile, 'r') as z:
         im = Image.open(z.open(found.file))
         nim = np.array(im)
         c = nim.shape[0] / 2, nim.shape[0] / 2
         plt.axis('off')
         fig=plt.imshow(im)
         fig.axes.get_xaxis().set_visible(False)
         fig.axes.get_yaxis().set_visible(False)
         plt.imshow(im)
         for i,[lat,lon] in enumerate(points):
             dx,dy=((lon-mapcenter[1])*scale[0],(lat-mapcenter[0])*scale[1])
             xx = c[0]+dx
             yy = c[1]+dy
             if xx > nim.shape[0] or yy > nim.shape[1] or xx<0 or yy<0: continue
             if i==0:
                 if bp: plt.plot(xx,yy,'rx')
                 else: plt.plot(xx,yy,'r,')
             else:
                 if pixel:
                     plt.plot(xx,yy,'r,')
                 else:
                     plt.plot(xx,yy,'r.')
         plt.savefig(outfile, bbox_inches='tight', pad_inches = 0, dpi = 300)

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

    conn = sqlite3.connect(params['elevdb'])
    c = conn.cursor()

    xi = np.unique([int(xx) for xx in x])
    yi = np.unique([int(yy) for yy in y])
    print (xi)
    print (yi)
    windows = []
    Ws = []
    for latint in yi:
        for lonint in xi:
            print (latint,lonint)
            sql = "SELECT latlow,lathigh,lonlow,lonhigh,W from RBF1 where latint=? and lonint=?"
            res = c.execute(sql,(int(latint),int(lonint)))
            for latlow,lathigh,lonlow,lonhigh,W in res:
                windows.append([latlow,lathigh,lonlow,lonhigh])
                Ws.append(W)

    windows = pd.DataFrame(windows)
    windows.columns = ['latlow','lathigh','lonlow','lonhigh']
    Ws = np.array(Ws)
    def nullfunc(d1,d2): return 0.0
    def isin(lat,lon):
        res = windows.apply(lambda x: \
                            lat>x.latlow  and \
                            lon>x.lonlow  and \
                            lat<x.lathigh and \
                            lon<x.lonhigh, \
                            axis=1)
        if np.any(res) == False: return nullfunc
        W = Ws[res]       
        rbfi = pickle.loads(W[0])
        return rbfi

    W = isin(lat1,lon1)
    xx,yy = np.meshgrid(x,y)
    zz = np.zeros(xx.shape)
    for i in range(xx.shape[0]):
        for j in range(xx.shape[1]):
            rbfi = isin(yy[i,j],xx[i,j])        
            znew = rbfi(xx[i,j],yy[i,j])
            if znew > 0.0: zz[i,j] = znew


    plon,plat = np.round(float(lon1),3),np.round(float(lat1),3)

    plt.figure()
    plt.plot(plon,plat,'rd')
    cs=plt.contour(xx,yy,zz,[100,300,400,500,700,1000,2000])
    plt.clabel(cs,inline=1,fontsize=9)
    plt.savefig(fout1)

    fig = plt.figure()
    ax = fig.gca(projection='3d')
    #ax.set_zlim3d(0, 2000)
    ax.view_init(elev=30,azim=250)
    ax.plot([plon],[plat],[1000],'r.')
    ls = LightSource(270, 45)
    rgb = ls.shade(zz, cmap=cm.gist_earth, vert_exag=0.1, blend_mode='soft')
    surf = ax.plot_surface(xx, yy, zz, rstride=1, cstride=1, facecolors=rgb, linewidth=0, antialiased=False, shade=False)
    plt.savefig(fout2)

    fig = plt.figure()
    ax = fig.gca(projection='3d')
    #ax.set_zlim3d(0, 2000)
    ax.view_init(elev=30,azim=40)
    ax.plot([plon],[plat],[1000],'r.')
    ls = LightSource(270, 45)
    rgb = ls.shade(zz, cmap=cm.gist_earth, vert_exag=0.1, blend_mode='soft')
    surf = ax.plot_surface(xx, yy, zz, rstride=1, cstride=1, facecolors=rgb, linewidth=0, antialiased=False, shade=False)
    plt.savefig(fout3)


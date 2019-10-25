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

params = json.loads(open(os.environ['HOME'] + "/Downloads/campdata/nomterr.conf").read())

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

    xx,yy = np.meshgrid(x,y)
    unique_latlon_ints = {}
    for (x,y) in zip(xx.flatten(),yy.flatten()):
        unique_latlon_ints[int(y),int(x)] = 1

    connmod = sqlite3.connect(params['elevdbmod'])
    k = list(unique_latlon_ints.keys())
    xis, nodes, epsilons = route.get_rbf_for_latlon_ints(k,connmod)
    
    pts = np.vstack((yy.flatten(),xx.flatten()))
    
    elevs = route.f_elev(pts.T, xis, nodes, epsilons)

    zz = []
    for (x,y) in zip(xx.flatten(),yy.flatten()):
        zz.append( elevs[(y,x)] )
    
    zz = np.array(zz)
    print (zz.shape)
    zz = zz.reshape(xx.shape)

    plon,plat = np.round(float(lon1),3),np.round(float(lat1),3)

    from scipy.ndimage.filters import gaussian_filter
    sigma = 0.7
    zz = gaussian_filter(zz, sigma)
    
    plt.figure()
    plt.plot(plon,plat,'rd')
    cs=plt.contour(xx,yy,zz,[200,300,400,500,700,900])
    plt.clabel(cs,inline=1,fontsize=9)
    plt.savefig(fout1)

    fig = plt.figure()
    ax = fig.gca(projection='3d')
    ax.view_init(elev=30,azim=250)
    ax.plot([plon],[plat],[np.max(zz)],'r.')
    ls = LightSource(270, 45)
    rgb = ls.shade(zz, cmap=cm.gist_earth, vert_exag=0.1, blend_mode='soft')
    surf = ax.plot_surface(xx, yy, zz, rstride=1, cstride=1, facecolors=rgb, linewidth=0, antialiased=False, shade=False)
    plt.savefig(fout2)

    fig = plt.figure()
    ax = fig.gca(projection='3d')
    ax.view_init(elev=30,azim=40)
    ax.plot([plon],[plat],[np.max(zz)],'r.')
    ls = LightSource(270, 45)
    rgb = ls.shade(zz, cmap=cm.gist_earth, vert_exag=0.1, blend_mode='soft')
    surf = ax.plot_surface(xx, yy, zz, rstride=1, cstride=1, facecolors=rgb, linewidth=0, antialiased=False, shade=False)
    plt.savefig(fout3)
    

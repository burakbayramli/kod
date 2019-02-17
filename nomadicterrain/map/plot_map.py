from mpl_toolkits.mplot3d import Axes3D
from scipy.spatial.distance import cdist
from matplotlib.colors import LightSource
from matplotlib import cm
import os, glob, re, zipfile
import pandas as pd, pickle
import numpy as np, sqlite3, json
import matplotlib.pyplot as plt
from PIL import Image
import geopy.distance
import pandas as pd, io

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

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

def get_centroid(poly):
    """Calculates the centroid of a non-intersecting polygon.
    Args:
        poly: a list of points, each of which is a list of the form [x, y].
    Returns:
        the centroid of the polygon in the form [x, y].
    Raises:
        ValueError: if poly has less than 3 points or the points are not
                    formatted correctly.
    """
    # Make sure poly is formatted correctly
    if len(poly) < 3:
        raise ValueError('polygon has less than 3 points')
    for point in poly:
        if type(point) is not list or 2 != len(point):
            raise ValueError('point is not a list of length 2')
    # Calculate the centroid from the weighted average of the polygon's
    # constituent triangles
    area_total = 0
    centroid_total = [float(poly[0][0]), float(poly[0][1])]
    for i in range(0, len(poly) - 2):
        # Get points for triangle ABC
        a, b, c = poly[0], poly[i+1], poly[i+2]
        # Calculate the signed area of triangle ABC
        area = ((a[0] * (b[1] - c[1])) +
                (b[0] * (c[1] - a[1])) +
                (c[0] * (a[1] - b[1]))) / 2.0;
        # If the area is zero, the triangle's line segments are
        # colinear so we should skip it
        if 0 == area:
            continue
        # The centroid of the triangle ABC is the average of its three
        # vertices
        centroid = [(a[0] + b[0] + c[0]) / 3.0, (a[1] + b[1] + c[1]) / 3.0]
        # Add triangle ABC's area and centroid to the weighted average
        centroid_total[0] = ((area_total * centroid_total[0]) +
                             (area * centroid[0])) / (area_total + area)
        centroid_total[1] = ((area_total * centroid_total[1]) +
                             (area * centroid[1])) / (area_total + area)
        area_total += area
    return centroid_total

def plot_topo(lat,lon,fout1,fout2,fout3):
    conn = sqlite3.connect(params['elevdb'])
    c = conn.cursor()
    
    sql = "SELECT W, gamma from RBF1 where ?>=latlow and ?<lathigh and ?>=lonlow and ?<lonhigh "
    res = c.execute(sql,(lat,lat,lon,lon))
    res = list(res)
    print ('len',len(res))
    if (len(res)!=1): raise Exception()
    W,gamma = res[0]
    df = pickle.loads(W)

    D=100
    xr=np.array(df[0])
    xr=xr.reshape(len(xr),1)
    yr=np.array(df[1])
    yr=yr.reshape(len(xr),1)
    X = np.hstack((xr,yr))

    x = np.linspace(np.min(xr),np.max(xr),D)
    y = np.linspace(np.min(yr),np.max(yr),D)
    xx,yy = np.meshgrid(x,y)
    xxx = xx.reshape(D*D)
    yyy = yy.reshape(D*D)

    tmp = np.vstack((xxx,yyy))
    d = cdist(X,tmp.T)

    znew = np.dot(df.w.T,np.exp(-gamma * d)).reshape(D,D)
    znew[znew<0] = 0

    plon,plat = np.round(float(lon),3),np.round(float(lat),3)
    
    fig = plt.figure()
    ax = fig.gca(projection='3d')
    #ax.set_zlim3d(0, 2000)
    ax.view_init(elev=30,azim=250)
    ax.plot([plon],[plat],[1000],'r.')
    ls = LightSource(270, 45)
    rgb = ls.shade(znew, cmap=cm.gist_earth, vert_exag=0.1, blend_mode='soft')
    surf = ax.plot_surface(xx, yy, znew, rstride=1, cstride=1, facecolors=rgb, linewidth=0, antialiased=False, shade=False)
    plt.savefig(fout1)

    fig = plt.figure()
    ax = fig.gca(projection='3d')
    #ax.set_zlim3d(0, 2000)
    ax.view_init(elev=30,azim=40)
    ax.plot([plon],[plat],[1000],'r.')
    ls = LightSource(270, 45)
    rgb = ls.shade(znew, cmap=cm.gist_earth, vert_exag=0.1, blend_mode='soft')
    surf = ax.plot_surface(xx, yy, znew, rstride=1, cstride=1, facecolors=rgb, linewidth=0, antialiased=False, shade=False)
    plt.savefig(fout2)
    
    plt.figure()
    plt.plot(plon,plat,'rd')
    cs=plt.contour(xx,yy,znew,20)
    plt.clabel(cs,inline=1,fontsize=9)
    plt.savefig(fout3)
         

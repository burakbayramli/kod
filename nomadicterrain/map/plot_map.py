import geopy.distance
import pandas as pd, io
from PIL import Image
import os, glob, re, zipfile
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

# enlem/boylam ve pikseller arasinda gecis icin
SCALEX = 2900. 
SCALEY = -4700.
dir = os.environ['HOME'] + '/Downloads/campdata/'
zfile = dir + 'europe2.zip'

def plot(points,outfile):
    """
    Birinci noktayi baz alarak gerekli harita inajini bul, ve diger
    tum noktalari bu harita uzerinde grafikle
    """
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
             dx,dy=((lon-mapcenter[1])*SCALEX,(lat-mapcenter[0])*SCALEY)             
             xx = c[0]+dx
             yy = c[1]+dy
             if xx > nim.shape[0] or yy > nim.shape[1] or xx<0 or yy<0: continue
             if i==0:
                 plt.plot(xx,yy,'rx')
             else:
                 plt.plot(xx,yy,'r.')
         plt.savefig(outfile, bbox_inches='tight', pad_inches = 0, dpi = 300)


def plot_area(pt, point_sets, outfile):
    """
    Birinci noktayi baz alarak gerekli harita inajini bul, ve diger
    tum noktalari etrafi cizgilerle belirli alan olarak ciz
    """
    center_res = pt
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
         for points in point_sets:
             print ('---')
             print (len(points))
             pixel_coords = []
             for i,[lat,lon] in enumerate(points):
                 dx,dy=((lon-mapcenter[1])*SCALEX,(lat-mapcenter[0])*SCALEY)             
                 xx = c[0]+dx
                 yy = c[1]+dy
                 if xx > nim.shape[0] or yy > nim.shape[1] or xx<0 or yy<0: continue
                 pixel_coords.append([xx,yy])

             print ('-----2')
             print (len(pixel_coords))
             for i in range(1,len(pixel_coords)):
                 plt.plot([pixel_coords[i-1][0],pixel_coords[i][0]],
                          [pixel_coords[i-1][1],pixel_coords[i][1]],
                          color='r',
                          linestyle='-',
                          linewidth=2)

             # line from the last point in the set to first one
             plt.plot([pixel_coords[-1][0],pixel_coords[0][0]],
                      [pixel_coords[-1][1],pixel_coords[0][1]],
                      color='r', linestyle='-',linewidth=2)
                 
         plt.savefig(outfile, bbox_inches='tight', pad_inches = 0, dpi = 300)




         

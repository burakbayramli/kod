import itertools, time
import pandas as pd, time
import numpy as np
import matplotlib.pyplot as plt
from math import sin, cos, sqrt, atan2, radians
from io import BytesIO
from PIL import Image
import urllib.request, os.path

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


def get_map(lat, lon, region, zoom):
    api = open("/home/burak/Documents/Dropbox/google_static_map_api.txt").read()
    url = "http://maps.googleapis.com/maps/api/staticmap?center=" + \
    	  "%f,%f&scale=2&size=800x800&maptype='terrain'&zoom=%d&key=%s" % (lat,lon,zoom,api)
    print (url)
    lats = str(lat).replace(".","_")
    lons = str(lon).replace(".","_")
    fout = "%s/%s_map_%s_%s.png" % (region,region,lats,lons)
    if os.path.isfile(fout):
        print ("Already downloaded...")
        return False
    buffer = BytesIO(urllib.request.urlopen(url).read())
    image = Image.open(buffer)
    image.save(fout)
    return True
    
def get_maps(c1,c2,px,py,region,zoom=11):
    """
    c1: one corner of the region box
    c2: the opposite corner of the region box

    get_maps will always pick the smallest / largest of each
    coord and create a box to sweep over. 
    """
    a= np.linspace(min(c1[0],c2[0]), max(c1[0],c2[0]), px)
    b= np.linspace(min(c1[1],c2[1]), max(c1[1],c2[1]), py)
    aa,bb = np.meshgrid(a,b)
    for x,y in zip(aa.flatten(),bb.flatten()):
        if get_map(x,y,region,zoom) == False: continue


if __name__ == "__main__":
    # harita hangi kordinatlar arasinda olmali
    c1 = (51.450320,2.963884); c2 = (39.460801, 29.786351)
    get_maps(c1, c2, 80, 80, region="europe2")

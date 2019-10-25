import numpy as np, plot_map, json, os
import matplotlib.pyplot as plt, quandl
import geopy.distance, math, route
from datetime import timedelta
import datetime
import pandas_datareader.data as web

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())
#print (params)

def test_map0():
    pts = [[36.54,32.0]]
    zfile,scale = params['mapzip']['turkey3']
    plot_map.plot(pts,'out.png',zfile=zfile,scale=scale)
    
def test_map3():
    zfile,scale = params['mapzip']['turkey1']
    pts = [[36.54,32.0],[36.64,32.1],[36.7,32.2], [36.84,32.3]]
    plot_map.plot(pts, 'out.png', zfile=zfile,scale=scale)
    
def test_dist_bearing():
    dist = geopy.distance.vincenty((51.215469, 4.427657),(51.218453, 4.431262))

    res = route.get_bearing((36.545471, 31.98567), (36.07653, 32.836227))
    assert (res > 90.0 and res < 180.0)
    print (res)
    
    res = route.get_bearing((51.215469, 4.427657), (51.218453, 4.431262))
    assert (res > 0.0 and res < 90.0)
    print (res)
    
    pt1 = (35.3354469, 33.309691)
    pt2 = (35.334978, 33.327677)
    res = route.get_bearing(pt1,pt2)
    print (res)
    assert (res > 85.0 and res < 95.0)
    
    print (dist)


    
def test_dist_to_segment():
    x1,y1=2,2
    x2,y2=5,5
    px,py=4,0
    res = route.dist(x1,y1, x2,y2, px,py)
    assert (res > 2.0 and res < 3.0)

def test_findata():
    bdays = 6000
    today = datetime.datetime.now()
    end_d=datetime.datetime(today.year, today.month, today.day)
    start_d = end_d - timedelta(days=bdays)
    df = web.DataReader("^GSPC", 'yahoo', start_d, end_d)
    df = df[['Close']]
    df.columns = ['SP500']
    print (len(df))
    print (df)

test_findata()
exit()
test_map0()
test_map3()
test_dist_bearing()
test_dijks()
test_dist_to_segment()


    

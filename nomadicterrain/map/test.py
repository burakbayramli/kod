import numpy as np, plot_map, json, os
import matplotlib.pyplot as plt
import geopy.distance, math, route

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())
#print (params)

def test_map0():
    pts = [[36.54,32.0]]
    zfile,scale = params['mapzip']['turkey3']
    plot_map.plot(pts,'out.png',zfile=zfile,scale=scale)

def test_map1():
    zfile,scale = params['mapzip']['turkey1']
    park1 = [[51.198689, 4.386747],[51.192246, 4.428266],[51.221485, 4.452641],[51.235894, 4.422429]]
    park2 = [[51.238689, 4.406747],[51.232246, 4.444266],[51.251485, 4.472641],[51.265894, 4.452429]]
    parks = [park1, park2]
    pt = [51.213052000000005,4.44]
    plot_map.plot_area(pt, parks, 'out.png', zfile=zfile,scale=scale)

def test_map2():
    zfile,scale = params['mapzip']['turkey1']
    park1 = [[43.031762, 19.045051],[43.070930, 18.999914], [43.101077, 18.935496], [43.161137, 18.982317], [43.261345, 18.978468], [43.233038, 19.110457], [43.273118, 19.114791], [43.156164, 19.317700], [43.079581, 19.398182], [42.998883, 19.478512], [42.960230, 19.398504], [43.016464, 19.386628], [43.099906, 19.343365], [43.161493, 19.237960], [43.201855, 19.150940], [43.197383, 19.093906], [43.167841, 19.067103], [43.152306, 19.097991], [43.131266, 19.106896], [43.087648, 19.097299], [43.058066, 19.102760], [43.047023, 19.073270], [43.029972, 19.056787], [43.066094, 19.039636], [43.062582, 19.009424], [43.067587, 18.973714]]
    park2 = [[42.903, 19.6414],[42.8989, 19.6472],[42.9014, 19.6767],[42.8959, 19.6956],[42.8808, 19.6925],[42.8627, 19.7038],[42.8531, 19.7025],[42.8382, 19.6767],[42.9051, 19.5549],[42.9273, 19.5739],[42.9185, 19.5923]]
    parks = [park1]
    pt = [51.2130605,4.4174822]
    plot_map.plot_area(pt, parks, 'out.png', zfile=zfile,scale=scale)
    
def test_map3():
    zfile,scale = params['mapzip']['turkey1']
    pts = [[36.54,32.0],[36.64,32.1],[36.7,32.2], [36.84,32.3]]
    plot_map.plot(pts, 'out.png', zfile=zfile,scale=scale)
    
def test_distance():
    dist = geopy.distance.vincenty((51.215469, 4.427657),(51.218453, 4.431262))
    print (route.get_bearing(36.545471, 31.98567, 36.07653, 32.836227) )
    print (route.get_bearing(51.215469, 4.427657, 51.218453, 4.431262))
    print (route.get_bearing(51.215469, 4.427657, 51.213319, 4.420361))
    print (route.get_bearing(51.215469, 4.427657, 51.218964, 4.419846))
    print (route.get_bearing(51.2130605,4.4174822, 51.213583, 4.424042))
    print (dist)

def test_dijks():
    m = np.array([[999.9, 999.9, 999.9,   0. ],
                  [999.9, 999.9, 999.9,   0. ],
                  [999.9, 999.9, 999.9,   0. ],
                  [  0.,    0.,    0.,    0. ]])
    
    res = route.dijkstra(m,(3,0),(0,3))
    exp = [(3, 0), (3, 1), (3, 2), (2, 3), (1, 3), (0, 3)]
    if exp != res: raise Exception("dijks")
    print (res)
    m = np.array([[999.9, 999.9, 999.9,   0. ],
                  [999.9, 999.9, 999.9,   0. ],
                  [999.9, 0,     999.9,   0. ],
                  [  0.,  -10.,    0.,    0. ]])
    
    res = route.dijkstra(m,(3,0),(0,3))
    exp = [(3, 0), (2, 1), (3, 2), (2, 3), (1, 3), (0, 3)]
    if exp != res: raise Exception("dijks")
    print (res)
    
def test_get_elev_data():
    lat1,lon1 = (36.545471, 31.98567)
    #lat2,lon2 = (36.545528, 32.142943)
    lat2,lon2 = (36.07653, 32.836227)    
    res = route.get_elev_data(lat1,lon1,lat2,lon2,npts=20)
    
#test_map0()
test_distance()
#test_dijks()
#test_get_elev_data()

    
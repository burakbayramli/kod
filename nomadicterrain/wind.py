import folium, numpy as np
import geopy.distance, pickle
import requests, json, os

def geo2arit(geo):
    if geo==360: geo=0
    if (geo>=0.0) and (geo <90.0): return 270.0-geo
    elif (geo>=90.0) & (geo<180.0): return 180.0-(geo-90)
    elif (geo>=180.0) & (geo<270.0): return 90.0-(geo-180)
    elif (geo>=270.0) & (geo<360.0): return 360.0-(geo-270)

def goto_from_coord(start, distance, bearing):
    s = geopy.Point(start[0],start[1])
    d = geopy.distance.geodesic(kilometers = distance)
    reached = d.destination(point=s, bearing=bearing)
    return [reached.latitude, reached.longitude]


def get_grid(lat,lon,dist=80):
    res1 = goto_from_coord((lat,lon),dist,45)
    res2 = goto_from_coord((lat,lon),dist,225)

    lowlat = np.min([res1[0],res2[0]])
    lowlon = np.min([res1[1],res2[1]])
    hilat = np.max([res1[0],res2[0]])
    hilon = np.max([res1[1],res2[1]])

    x = np.linspace(lowlon,hilon,5)
    y = np.linspace(lowlat,hilat,5)

    xx,yy = np.meshgrid(x,y)
    return yy.flatten(), xx.flatten()

def get_data(lat,lon,weatherapi):
    print (lat,lon)
    base_url = 'http://api.openweathermap.org/data/2.5/forecast?'
    payload = { 'lat': str(lat), 'lon': str(lon), 'units': 'metric', 'APPID': weatherapi }
    r = requests.get(base_url, params=payload)
    wind = []
    rain = []
    for x in r.iter_lines():
    	x = json.loads(x.decode())
    	for i,xx in enumerate(x['list']):
            wind.append((xx['dt_txt'], xx.get('wind') ))
            rain.append((xx['dt_txt'], xx.get('rain') ))

    return wind, rain

def get_data_multi(lats,lons):
    dwind = {}
    drain = {}
    params = json.loads(open(os.environ['HOME'] + "/.nomterr.conf").read())
    weatherapi = params['weatherapi']
    for lat,lon in zip(lats,lons):
    	wind, rain = get_data(lat,lon,weatherapi)
    	dwind[(lat,lon)] = wind
    	drain[(lat,lon)] = rain

    return dwind, drain

def testdata():
    lat,lon=10.7901,86.4303
    lats,lons = get_grid(lat,lon,dist=800)
    dwind,drain = get_data_multi(lats,lons)
    pickle.dump(dwind, open('wind.pkl', 'wb'))

def plot_wind(lat,lon,timeindex,wide,fout='/tmp/wind.html'):
    lats,lons = get_grid(lat,lon,dist=wide)
    dwind,drain = get_data_multi(lats,lons)    
    u = []; v = []; r = []
    speeds = []
    for lat1,lon1 in zip(lats,lons):
        #rain
        t2, rain = drain[(lat1,lon1)][timeindex]
        if rain != None: r.append(float(rain['3h']))
        if rain == None: r.append(0.0)
        #wind
        t, d = dwind[(lat1,lon1)][timeindex]
        speeds.append(float(d['speed']))
        S = float(d['speed']) / (800/wide)
        tmpu = S * np.cos(np.deg2rad(geo2arit(float(d['deg']))))
        u.append(tmpu)
        tmpv = S * np.sin(np.deg2rad(geo2arit(float(d['deg']))))
        v.append(tmpv)
        
    u = np.array(u)
    v = np.array(v)
    r = np.array(r)
    speeds = np.array(speeds)
    stime = t
    m = folium.Map(location=[lat,lon],tiles='Stamen Terrain',zoom_start=8)
    for i in range(len(lons)):
        coordinates=[(lats[i],lons[i]),(lats[i]+v[i],lons[i]+u[i])]
        folium.PolyLine(locations=coordinates,weight=2,color = 'blue').add_to(m)
        folium.CircleMarker(location=(lats[i]+v[i],lons[i]+u[i]),
                            fill_color='blue', radius=10).add_to(m)


    stitle = "Max %.2f m/sec , Rain %.2f %s" % (np.max(speeds),np.mean(r),str(stime)) 
    stitle = "<h3>" + stitle + "</h3>"
    m.get_root().html.add_child(folium.Element(stitle))
    m.save(fout)
    
    
if __name__ == "__main__": 
    lat,lon=9.6224,64.6930
    plot_wind(lat,lon,0,20) # 0,2,6,22

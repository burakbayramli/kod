import matplotlib.pyplot as plt
import cartopy.crs as ccrs
import numpy as np
import matplotlib.pyplot as plt
import geopy.distance
import requests, json

def geo2arit(geo):
    if (geo>=0.0) & (geo <90.0): return 270.0-geo
    elif (geo>=90.0) & (geo<180.0): return 180.0-(geo-90)
    elif (geo>=180.0) & (geo<270.0): return 90.0-(geo-180)
    elif (geo>=270.0) & (geo<360.0): return 360.0-(geo-270)

def goto_from_coord(start, distance, bearing):
    s = geopy.Point(start[0],start[1])
    d = geopy.distance.geodesic(kilometers = distance)
    reached = d.destination(point=s, bearing=bearing)
    return [reached.latitude, reached.longitude]


def get_grid(lat,lon):
    dist = 80
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

def get_data(lat,lon):
    base_url = 'http://api.openweathermap.org/data/2.5/forecast?'
    weatherapi = "04846fdd7288b5f5ebd8f091b106140e"
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
    for lat,lon in zip(lats,lons):
    	wind, rain = get_data(lat,lon)
    	dwind[(lat,lon)] = wind
    	drain[(lat,lon)] = rain

    return dwind, drain


def plot_wind(lat, lon, lats, lons, dwind, drain, timeindex, fout):
    fig = plt.figure()
    ax = fig.add_subplot(1, 1, 1, projection=ccrs.PlateCarree())
    ax.set_global()
    ax.stock_img()
    ax.coastlines()

    u = []; v = []; r = []
    for lat1,lon1 in zip(lats,lons):
        t2, rain = drain[(lat1,lon1)][timeindex]
        if rain != None: r.append(float(rain['3h']))
        if rain == None: r.append(0.0)
        t, d = dwind[(lat1,lon1)][timeindex]
        tmpu = float(d['speed']) * np.cos(np.deg2rad(geo2arit(float(d['deg']))))
        u.append(tmpu)
        tmpv = float(d['speed']) * np.sin(np.deg2rad(geo2arit(float(d['deg']))))
        v.append(tmpv)
        #print ('------', d['speed'], d['deg'], geo2arit(float(d['deg'])), tmpu, tmpv  )

    u = np.array(u)
    v = np.array(v)
    r = np.array(r)

    print (r)
    stitle = " Max %.2f Units, Rain %.2f" % (np.abs(np.max(u)), np.mean(r) )
   
    ax.set_extent([int(lon-1), int(lon)+2, int(lat)-1, int(lat)+2])
    gl = ax.gridlines(crs=ccrs.PlateCarree(), draw_labels=True, linewidth=0)
    ax.quiver(lons, lats, u, v)
    ax.set_title(t + stitle)
    plt.savefig(fout)

if __name__ == "__main__": 


    # 40.843432,29.92634
    lat,lon=40.84343206497589, 29.926342357515754
    #lat,lon=38.784420553872785, 17.730192742377437
    #lat,lon=44.0564831111056, -10.066920538621517
    lats,lons = get_grid(lat,lon)
    dwind,drain = get_data_multi(lats,lons)
    plot_wind(lat, lon, lats, lons, dwind, drain, 0, '/tmp/har-0.png')
    plot_wind(lat, lon, lats, lons, dwind, drain, 2, '/tmp/har-2.png')
    plot_wind(lat, lon, lats, lons, dwind, drain, 6, '/tmp/har-6.png')

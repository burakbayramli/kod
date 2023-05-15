from pygeodesy.sphericalNvector import LatLon
import matplotlib.pyplot as plt, json, polyline
import numpy as np, folium, requests
import gpxpy, gpxpy.gpx
import numpy as np

def midpoint(fr,to):
    b = LatLon(fr[0], fr[1]), LatLon(to[0], to[1])
    nvecs = np.array([a.toNvector() for a in b])
    mid = nvecs.mean().toLatLon()
    return (mid.lat,mid.lon)

def dist(fr,to):
    p1 = LatLon(fr[0], fr[1])
    p2 = LatLon(to[0], to[1])
    return p1.distanceTo(p2)
    
def create_gpx(coords, outfile):
    fout = open(outfile, "w")
    gpx = gpxpy.gpx.GPX()
    gpx_track = gpxpy.gpx.GPXTrack()
    gpx.tracks.append(gpx_track)
    gpx_segment = gpxpy.gpx.GPXTrackSegment()
    gpx_track.segments.append(gpx_segment)
    for c in coords:
        gpx_segment.points.append(gpxpy.gpx.GPXTrackPoint(c[0], c[1], elevation=0))
    fout.write(gpx.to_xml())
    fout.close()

def create_folium(lat1,lon1,coords,fout):
    map = folium.Map(location=(lat1,lon1),zoom_start=8,control_scale=True)
    folium.PolyLine(locations=coords, color="blue").add_to(map)    
    map.save(fout)    

def create_osrm_folium(lat1,lon1,lat2,lon2,fout):    
    url = f'http://router.project-osrm.org/route/v1/car/' + \
          f'{lon1},{lat1};{lon2},{lat2}' + \
          f'?alternatives=false'
    response = requests.get(url, verify=False)
    resp = json.loads(response.text)
    decoded = polyline.decode(resp["routes"][0]['geometry'])
    map = folium.Map(location=(lat1,lon1),zoom_start=8,control_scale=True)
    folium.PolyLine(locations=decoded, color="blue").add_to(map)    
    map.save(fout)        

def create_osrm_gpx(lat1,lon1,lat2,lon2):    
    url = f'http://router.project-osrm.org/route/v1/car/' + \
          f'{lon1},{lat1};{lon2},{lat2}' + \
          f'?alternatives=false'
    response = requests.get(url, verify=False)
    resp = json.loads(response.text)
    decoded = polyline.decode(resp["routes"][0]['geometry'])
    return decoded
    
if __name__ == "__main__": 
  fr = (40.969615352945354,29.07036154764545)
  to = (40.96660865138665,29.086701750114123)
  mid = midpoint(fr,to)
  d = dist(fr,to)
  path = get_path(fr,to,d*2)
  create_osmnx_gpx(path, "/tmp/out.gpx")
  

from pygeodesy.sphericalNvector import LatLon
import numpy as np, folium
import gpxpy, gpxpy.gpx
import matplotlib.pyplot as plt
import numpy as np
import osmnx as ox

def midpoint(fr,to):
    b = LatLon(fr[0], fr[1]), LatLon(to[0], to[1])
    nvecs = np.array([a.toNvector() for a in b])
    mid = nvecs.mean().toLatLon()
    return (mid.lat,mid.lon)

def dist(fr,to):
    p1 = LatLon(fr[0], fr[1])
    p2 = LatLon(to[0], to[1])
    return p1.distanceTo(p2)

def get_path(fr,to,d):
    ox.config(use_cache=True, cache_folder='/tmp/osmnx')
    G = ox.graph_from_point(fr, dist=d, network_type="walk")
    origin_res = ox.get_nearest_node(G, fr, method='euclidean',return_dist=True)
    destination_res = ox.get_nearest_node(G, to, method='euclidean',return_dist=True)
    route = ox.shortest_path(G, origin_res[0], destination_res[0])
    coords = [[G.nodes[r]['y'],G.nodes[r]['x']] for r in route]
    return coords
    
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
        
if __name__ == "__main__": 
  fr = (40.969615352945354,29.07036154764545)
  to = (40.96660865138665,29.086701750114123)
  mid = midpoint(fr,to)
  d = dist(fr,to)
  path = get_path(fr,to,d*2)
  create_gpx(path, "/tmp/out.gpx")
  

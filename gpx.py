import gpxpy, gpxpy.gpx, pandas as pd, numpy as np

#arr = [[41.32812717252302, 28.73793868206552]]
arr = np.array(pd.read_csv("/tmp/in.csv",header=None))

gpx = gpxpy.gpx.GPX()

gpx_track = gpxpy.gpx.GPXTrack()
gpx.tracks.append(gpx_track)

gpx_segment = gpxpy.gpx.GPXTrackSegment()
gpx_track.segments.append(gpx_segment)

for lat,lon in arr:
    gpx_segment.points.append(gpxpy.gpx.GPXTrackPoint(lat, lon, elevation=0))

fout = open("/tmp/out.gpx","w")
fout.write(gpx.to_xml())
fout.close()

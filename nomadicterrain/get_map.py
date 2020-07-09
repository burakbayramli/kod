from PIL import Image
import subprocess, os, json
import numpy as np

print (os.getcwd())

def get_map(lat, lon, zoom):
    pts = [[lat,lon]]
    spts = str([str(pt[0]) + ";" + str(pt[1]) for pt in pts])
    spts = spts.replace('[','').replace(']','')
    spts = spts.replace("'","").replace(" ","")
    print (spts)
    cmd = ['/bin/sh',os.environ['HOME']+'/Documents/kod/nomadicterrain/map/staticmap/run.sh',
           spts,
           '/home/burak/Downloads',
           '/home/burak/Downloads/campdata/turkey.map',
           str(zoom)]
    result = subprocess.run(cmd, stdout=subprocess.PIPE)
    print (result.stdout)
    s = result.stdout.decode('utf-8')
    print (s)
    center,fout = s.split(';')
    lat,lon = center.split(",")
    print (lat,lon)
    print (fout)

    imgout = lat.replace(".","_") + "_" + lon.replace(".","_") 
    cmd = ['/usr/bin/convert', '-scale', '70%', fout, '/tmp/marmara2/%s.jpg' % imgout]
    print (cmd)
    result = subprocess.run(cmd, stdout=subprocess.PIPE)

def get_maps():
    #zoom = 13
    zoom = 10
    lonmin,latmin,lonmax,latmax=25,38,32,42
    for latint in range(int(latmin),int(latmax)):
        for lonint in range(int(lonmin),int(lonmax)):
            for declat in np.linspace(0,1,20):
                for declon in np.linspace(0,1,20):
                    lat,lon = float(latint) + declat, float(lonint) + declon
                    print (lat,lon)
                    get_map(lat,lon,zoom)
    
if __name__ == "__main__":

    get_maps()
    
    #zoom = 13
    #lat,lon=40.970041,29.070311
    #get_map(lat,lon,zoom)
    #lat,lon=40.965634, 29.093566
    #get_map(lat,lon,zoom)
    #40.966282, 29.092686 test this
    

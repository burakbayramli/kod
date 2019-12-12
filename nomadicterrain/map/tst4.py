import subprocess, os, json
print (os.getcwd())

lat1,lon1=40.970041,29.070311
lat2,lon2=40.971041,29.071311
lat3,lon3=40.968254,29.080640
pts = [[lat1,lon1],[lat2,lon2],[lat3,lon3]]
spts = str([str(pt[0]) + ";" + str(pt[1]) for pt in pts])
spts = spts.replace('[','').replace(']','')
spts = spts.replace("'","").replace(" ","")
print (spts)
cmd = ['/bin/sh',os.environ['HOME']+'/Documents/kod/nomadicterrain/map/staticmap/run2.sh',
       spts,
       '/data/data/com.termux/files/home/Downloads',
       '/data/data/com.termux/files/home/Downloads/campdata/turkey.map',
       '14']
result = subprocess.run(cmd, stdout=subprocess.PIPE)
print (result)
#res = json.loads(result.stdout.decode('utf-8'))
#print ('pix',res['pixels'])
#print (res['file'])

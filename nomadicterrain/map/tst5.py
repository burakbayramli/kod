import subprocess, os, json
print (os.getcwd())

lat1,lon1=40.970041,29.070311
pts = [[lat1,lon1]]
spts = str([str(pt[0]) + ";" + str(pt[1]) for pt in pts])
spts = spts.replace('[','').replace(']','')
spts = spts.replace("'","").replace(" ","")
print (spts)
cmd = ['/bin/sh',os.environ['HOME']+'/Documents/kod/nomadicterrain/map/staticmap/run.sh',
       spts,
       '/home/burak/Downloads',
       '/home/burak/Downloads/maps/turkey.map',
       '14']
result = subprocess.run(cmd, stdout=subprocess.PIPE)
print (result.stdout)
s = result.stdout.decode('utf-8')
center,fout = s.split(';')
print (center.split(","))
print (fout)

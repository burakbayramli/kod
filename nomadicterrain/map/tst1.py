import subprocess, os, json
os.chdir("/home/burak/Documents/kod/nomadicterrain/map/staticmap")
print (os.getcwd())
cmd = ['java','-cp','build:lib/kxml2-2.3.0.jar:lib/mapsforge-core-0.12.0.jar:lib/mapsforge-map-0.12.0.jar:lib/mapsforge-map-awt-0.12.0.jar:lib/mapsforge-map-reader-0.12.0.jar:lib/mapsforge-themes-0.12.0.jar:lib/svg-salamander-1.0.jar','SaveTiles', '40.970041,29.070311;40.971041,29.071311;40.968254,29.080640','/tmp']
result = subprocess.run(cmd, stdout=subprocess.PIPE)
#print (result.stdout)
res = json.loads(result.stdout.decode('utf-8'))
print (res['pixels'])
print (res['file'])


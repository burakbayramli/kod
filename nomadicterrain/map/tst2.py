import subprocess, os, json
print (os.getcwd())
cmd = ['/bin/sh','/home/burak/Documents/kod/nomadicterrain/map/staticmap/run.sh', '40.970041,29.070311;40.971041,29.071311;40.968254,29.080640','/tmp']
result = subprocess.run(cmd, stdout=subprocess.PIPE)
res = json.loads(result.stdout.decode('utf-8'))
print (res['pixels'])
print (res['file'])

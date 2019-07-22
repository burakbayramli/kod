import json, sqlite3, os

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

conn = sqlite3.connect(params['elevdb'])
c = conn.cursor()
sql1 = "SELECT count(*) FROM ELEVATION WHERE latint=%d and lonint=%d and elevation is NULL" % (41,22)
res = c.execute(sql1)
for x in res: print (x)
conn.close()


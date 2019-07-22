import sqlite3
cs = "/media/burak/Seagate Backup Plus Drive/archive/data/campdata/elev.db"
#cs = "/media/burak/1BC3-0618/archive/data/campdata/elev.db"
conn = sqlite3.connect(cs)
c = conn.cursor()
#latint,lonint = 52,-5
latint,lonint = 34,19
sql = "SELECT count(*) FROM ELEVATION WHERE latint=%d and lonint=%d" % (latint,lonint)
res = c.execute(sql)
res = list(res)
print (res)

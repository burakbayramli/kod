import json, sqlite3, os

coords = {}

longmin,latmin,longmax,latmax =  2.553,56.15,31.833,71.5
print (longmin,latmin,longmax,latmax)
for lonint in (range(int(longmin),int(longmax)+1)):
    for latint in (range(int(latmin),int(latmax)+1)):
        coords[(latint,lonint)] = 1

longmin,latmin,longmax,latmax =  18.0,60.203,33.74,70.083
print (longmin,latmin,longmax,latmax)
for lonint in (range(int(longmin),int(longmax)+1)):
    for latint in (range(int(latmin),int(latmax)+1)):
        coords[(latint,lonint)] = 1

longmin,latmin,longmax,latmax =  -10.653761,49.955441,6.709314,58.701033
print (longmin,latmin,longmax,latmax)
for lonint in (range(int(longmin),int(longmax)+1)):
    for latint in (range(int(latmin),int(latmax)+1)):
        coords[(latint,lonint)] = 1

longmin,latmin,longmax,latmax =  -10.683,51.367,-4.0,55.433
print (longmin,latmin,longmax,latmax)
for lonint in (range(int(longmin),int(longmax)+1)):
    for latint in (range(int(latmin),int(latmax)+1)):
        coords[(latint,lonint)] = 1

#print (coords)

connmain = sqlite3.connect('/home/burak/Downloads/elev.db')

def delete(latint,lonint):
    c = connmain.cursor()
    sql = "DELETE FROM ELEVATION WHERE latint=%d and lonint=%d" % (latint,lonint)
    print (sql)
    c.execute(sql)
    connmain.commit()        

sql = "SELECT distinct latint,lonint FROM ELEVATION "
res = list(connmain.execute(sql))
for (latint,lonint) in res:

    sql = "SELECT avg(elevation) FROM ELEVATION WHERE latint=%d and lonint=%d" % (latint,lonint)
    res = connmain.execute(sql)
    res = list(res)
    print(res[0][0])
    if res[0][0] < 0.0: 
        delete (latint,lonint)
        
    print (latint,lonint)    
    if (latint,lonint) not in coords:
        delete (latint,lonint)

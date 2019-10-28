import json, sqlite3

#conn = sqlite3.connect('/home/burak/Downloads/elev5.db')
#c = conn.cursor()
#c.execute('''CREATE INDEX LATLON1 ON ELEVATION (lat,lon); ''')
#c.execute('''CREATE INDEX LATLON2 ON ELEVATION (latint,lonint,elevation); ''')
#conn.close()

conn = sqlite3.connect('/home/burak/Downloads/campdata/elevmod.db')
c = conn.cursor()
c.execute('''CREATE INDEX RBF1 ON ELEVRBF (latint,lonint); ''')
c.execute('''CREATE INDEX RBF2 ON ELEVRBF (latint,lonint,lati,lonj); ''')
conn.close()


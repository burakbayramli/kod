import json, sqlite3

conn = sqlite3.connect('/home/burak/Downloads/elev4.db')
c = conn.cursor()
c.execute('''CREATE TABLE ELEVATION (latint INT, lonint INT, lat REAL, lon REAL, elevation REAL); ''')

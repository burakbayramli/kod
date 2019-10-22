import json, sqlite3

conn = sqlite3.connect('/home/burak/Downloads/campdata/elevmod.db')
c = conn.cursor()
c.execute('''CREATE TABLE ELEVRBF (latint INT, lonint INT, lati INT, lonj INT, W BLOB); ''')

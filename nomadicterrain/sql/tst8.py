import json, sqlite3

conn = sqlite3.connect('/home/burak/Downloads/elev.db', isolation_level=None)
conn.execute("VACUUM")
conn.close()

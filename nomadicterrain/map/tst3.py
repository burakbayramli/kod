import geopy.distance, numpy as np

f = 'gps_coord_sample.npy'

t = np.load(f)

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

conn = sqlite3.connect(params['elevdb')
c = conn.cursor()
res = c.execute('''CREATE TABLE ELEVATION (latint INT, lonint INT, lat REAL, lon REAL, elevation REAL); ''')

#insert = "INSERT INTO BOOKS(path,content) VALUES('%s','%s');"
#c.execute(insert % ("book1.pdf",'SQLite is a software...'))



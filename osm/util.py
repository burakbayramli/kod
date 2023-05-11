import os, sqlite3

class SQLDefaultDictDict(dict):

    def __init__(self,dbfile):
        if os.path.exists(dbfile) == False:
            self.conn = sqlite3.connect(dbfile)
            res = c.execute('''CREATE TABLE OBJ (key TEXT PRIMARY KEY, value TEXT); ''')
            self.conn.commit()    

    def __getattr__(self, item):
        c = self.conn.cursor()
        c.execute("SELECT value FROM OBJ WHERE key = ?", [key])
        res = list(c.fetchall())
        if len(res)==0: 
            return None
        res = pickle.loads(base64.decodestring(res[0].encode('utf-8')))        

    def __setattr__(self, key, value):
        c = self.conn.cursor()
        value = base64.encodestring(pickle.dumps(value)).decode()
        c.execute("insert or replace into OBJ (key,value) values (?,?)", [key,value])


d = SQLDefaultDictDict("out.sqlite")

d[324234] = 3424
d['aaaa'] = 111

print (d[324234])
print (d['aaaa'])


    

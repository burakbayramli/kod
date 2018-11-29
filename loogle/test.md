

```python
import sqlite3

conn = sqlite3.connect('taban.db')
c = conn.cursor()
c.execute('''DROP TABLE DOCS ; ''')
c.execute('''CREATE VIRTUAL TABLE DOCS USING fts3(content TEXT); ''')
c.execute('''INSERT INTO DOCS(content) VALUES('All doctor  source code...'); ''')
c.execute('''INSERT INTO DOCS(content) VALUES('All his SQLite source code...'); ''')
c.execute('''INSERT INTO DOCS(content) VALUES('All SQLite source where...'); ''')
c.execute('''INSERT INTO DOCS(content) VALUES('All SQLite his doctor code...'); ''')
conn.commit()
conn.close()
```


```python
conn = sqlite3.connect('taban.db')
c = conn.cursor()
c.execute('''SELECT * FROM DOCS WHERE content MATCH "his source";''')
rows = c.fetchall()
for r in rows: print(r)
```

```text
('All his SQLite source code...',)
```

```python
index_db = "/home/burak/Downloads/books.db"
conn = sqlite3.connect(index_db)
c = conn.cursor()
#c.execute('''SELECT * FROM BOOKS WHERE content MATCH "scientist";''')
c.execute('''SELECT path,size FROM BOOKS;''')
rows = c.fetchall()
for r in rows: print(r)
```

```text
('/home/burak/Documents/kod/loogle/sub/a1.pdf', '17179')
('/home/burak/Documents/kod/loogle/sub/tmp.pdf', '90392')
('/home/burak/Documents/kod/loogle/sub/sub2/b1.pdf', '15666')
('/home/burak/Documents/kod/loogle/sub/sub3/veryveryimpo.txt', '103')
('/home/burak/Documents/kod/loogle/sub/sub3/b2.pdf', '14989')
```

```python
index_db = "/home/burak/Downloads/books.db"
conn = sqlite3.connect(index_db)
c = conn.cursor()
path = '/home/burak/Documents/kod/loogle/sub/sub3/b2.pdf'
c.execute('''SELECT count(*) FROM BOOKS where path = '%s' ''' % path)
rows = c.fetchall()
print (rows[0][0])
```

```text
1
```

```python
import sqlite3
index_db = "/home/burak/Downloads/loogle.db"
conn = sqlite3.connect(index_db)
c = conn.cursor()
c.execute('''SELECT path,size FROM BOOKS;''')
rows = c.fetchall()
print (rows)
```


```python
import sqlite3
index_db = "/home/burak/Downloads/loogle.db"
conn = sqlite3.connect(index_db)
c = conn.cursor()
c.execute('''SELECT count(*) FROM BOOKS;''')
rows = c.fetchall()
print (rows)
```

```text
[(1409,)]
```




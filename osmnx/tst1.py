from sqlitedict import SqliteDict
db = SqliteDict("example.sqlite")

db["1"] = {"name": "first item"}
db["2"] = {"name": "second item"}
db["3"] = {"name": "yet another item"}

# Commit to save the objects.
db.commit()

print (db["3"])


from sqlitedict import SqliteDict

dd = SqliteDict("walkdict.sqlite")

print (dd[2377631845])
print (dd[1364308852])
print (dd['2377631845'])
print (dd['1364308852'])


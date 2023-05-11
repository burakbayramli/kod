import pickle

dd = pickle.load(open("walkdict2.pkl","rb"))

print (dd[2377631845])
print (dd[1364308852])
print (dd['2377631845'])
print (dd['1364308852'])


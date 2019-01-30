from pqdict import pqdict
pq = pqdict({'a':3, 'b':5, 'c':8})
for i in range(100000):
    print (i)
    pq[(i,i)] = i

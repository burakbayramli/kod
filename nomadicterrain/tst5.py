from priodict import priorityDictionary # onemsel kuyruk
import numpy as np

def get_neighbor_idx(x,y,dims):
    res = []
    for i in ([0,-1,1]):
        for j in ([0,-1,1]):
            if i==0 and j==0: continue
            if x+i<(dims[0]) and x+i>-1 and y+j<(dims[1]) and y+j>-1:
                res.append((x+i,y+j))
    return res

def dijkstra(C,s,e):    
    D = {}  
    Q = priorityDictionary() 
    Q[s] = 0
    for v in Q:
       D[v] = Q[v]
       print ('v',v)
       print ('e',e)
       #if str(v) == str(e): break      
       neighs = get_neighbor_idx(v[0],v[1],[4,4])
       print (neighs)
       for n in neighs:
          w = np.abs(C[n[0],n[1]] - C[v[0],v[1]])
          print (w)

C = np.ones((4,4)) * 999.9
C[:,-1] = 0.0
C[-1,:] = 0.0
print (C)
#neighs = get_neighbor_idx(3,0,[4,4])
#print (neighs)
dijkstra(C,(3,0),(0,3))

import geopy.distance, numpy as np

f = 'gps_coord_sample.npy'

t = np.load(f)
print (t[5381])
exit()

M=1000
S=40000
res = np.zeros((M*M,2))
k=0
for i in range(M):
    for j in range(M):
        res[k,0] = i*0.001
        res[k,1] = j*0.001
        k+=1

idx = range(M*M)
        
sample_idx = np.random.choice(idx, S, replace=False)

sample=res[idx]

np.save(f,sample)

#print (geopy.distance.vincenty((24.0,31.0),(24.001,31.00)))
#print (geopy.distance.vincenty((24.0,31.0),(24.00,31.001)))


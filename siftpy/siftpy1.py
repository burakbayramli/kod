import numpy.linalg as lin
import siftimp, os
import pandas as pd
import numpy as np

def sift(fin, threshold=10.0):
    fout = "/tmp/" + fin.replace(".pgm",".key")
    if os.path.exists(fout): os.remove(fout)
    res = siftimp.sift_imp(fin, str(threshold))
    df = pd.read_csv(fout,sep=' ',header=None)
    return df

def match(df1,df2,dist_ratio=0.7):
    desc1 = np.array(df1)[:,4:]
    desc2 = np.array(df2)[:,4:]
    desc1 = np.array([d/lin.norm(d) for d in desc1])
    desc2 = np.array([d/lin.norm(d) for d in desc2])
    
    desc1_size = desc1.shape
    
    matchscores = np.zeros((desc1_size[0]),'int')
    desc2t = desc2.T # precompute matrix transpose
    for i in range(desc1_size[0]):
        dotprods = np.dot(desc1[i,:],desc2t) # vector of dot products
        dotprods = 0.9999*dotprods
        # inverse cosine and sort, return index for features in second image
        indx = np.argsort(np.arccos(dotprods))
        
        # check if nearest neighbor has angle less than dist_ratio times 2nd
        if np.arccos(dotprods)[indx[0]] < dist_ratio * np.arccos(dotprods)[indx[1]]:
            matchscores[i] = int(indx[0])
    
    return matchscores
        
def match_twosided(desc1,desc2):
    matches_12 = match(desc1,desc2)
    matches_21 = match(desc2,desc1)
    
    ndx_12 = matches_12.nonzero()[0]
    
    # remove matches that are not symmetric
    for n in ndx_12:
        if matches_21[int(matches_12[n])] != n:
            matches_12[n] = 0
    
    return matches_12


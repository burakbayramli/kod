#
# Recommend movies based on Grouplens ratings filee
#
# https://grouplens.org/datasets/movielens/latest/
#
# Download the full file, and unzip in a known
# location update the d variable below
#

from sklearn.metrics.pairwise import cosine_similarity
from scipy.sparse import csr_matrix
import scipy.sparse.linalg
import pandas as pd, numpy as np
import os, sys, re

d = "/media/burak/3d1ece2f-6539-411b-bac2-589d57201626/home/burak/Downloads/ml-latest"

picks = {"Star Trek: First Contact (1996)": 5.0,
         "Assassins (1995)": 5.0,
         "Tombstone (1993)": 5.0
}

if len(sys.argv) < 2:
    print ("Usage movrecom.py [normrec|svdrec]")
    exit()

if sys.argv[1] == "normal":
    ratings = pd.read_csv(d + "/ratings.csv")
    utility_csr = csr_matrix((ratings.rating, (ratings.userId , ratings.movieId)))

    mov = pd.read_csv(d + "/movies.csv",index_col="title")['movieId'].to_dict()
    tst = np.zeros((1,utility_csr.shape[1]))
    for p in picks: tst[0,mov[p]] = picks[p]

    similarities = cosine_similarity(utility_csr, tst)

    m = np.argsort(similarities[:,0])
    movi = pd.read_csv(d + "/movies.csv",index_col="movieId")['title'].to_dict()

    res = {}
    for idx in range(1,1000):
        ii,jj = utility_csr[m[-idx],:].nonzero()    
        for j in jj:
            r = utility_csr[m[-idx],:][0,j]
            n = movi[j]
            if n not in picks and r >= 4.0: res[n] = r 
    for x in res: print (x)

if sys.argv[1] == "svd":
    ratings = pd.read_csv(d + "/ratings.csv")
    utility_csr = csr_matrix((ratings.rating, (ratings.userId , ratings.movieId)))

    mov = pd.read_csv(d + "/movies.csv",index_col="title")['movieId'].to_dict()

    for p in picks: utility_csr[0,mov[p]] = picks[p]
    
    A = scipy.sparse.linalg.svds(utility_csr, k=10)[0]

    similarities = cosine_similarity(A, A[0,:].reshape(1,10))
    m = np.argsort(similarities[:,0])
    movi = pd.read_csv(d + "/movies.csv",index_col="movieId")['title'].to_dict()

    res = {}
    for idx in range(1,100):
        ii,jj = utility_csr[m[-idx],:].nonzero()    
        for j in jj:
            r = utility_csr[m[-idx],:][0,j]
            n = movi[j]
            year = int(re.findall('(\d\d\d\d)', n)[0])
            if n not in picks and r >= 4.0 and year>1990: res[n] = r 
    for x in res: print (x)    

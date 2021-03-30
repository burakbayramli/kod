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
import pandas as pd, numpy as np
import os, sys

d = "/media/burak/3d1ece2f-6539-411b-bac2-589d57201626/home/burak/Downloads/ml-latest"

if len(sys.argv) < 2:
    print ("Usage movrecom.py [action]")
    exit()

if sys.argv[1] == "rec":
    ratings = pd.read_csv(d + "/ratings.csv")
    utility_csr = csr_matrix((ratings.rating, (ratings.userId , ratings.movieId)))

    mov = pd.read_csv(d + "/movies.csv",index_col="title")['movieId'].to_dict()
    picks = {"Swordfish (2001)": 5.0, "Every Which Way But Loose (1978)": 5.0,
             "Sideways (2004)": 5.0, "Expendables, The (2010)": 5.0
    }
    tst = np.zeros((1,utility_csr.shape[1]))
    for p in picks: tst[0,mov[p]] = picks[p]

    similarities = cosine_similarity(utility_csr, tst)
    m = np.argsort(similarities[:,0])
    movi = pd.read_csv(d + "/movies.csv",index_col="movieId")['title'].to_dict()

    res = {}
    for idx in range(1,20):
        ii,jj = utility_csr[m[-idx],:].nonzero()    
        for j in jj:
            r = utility_csr[m[-idx],:][0,j]
            n = movi[j]
            if n not in picks and r >= 4.0: res[n] = r 
    for x in res: print (x)



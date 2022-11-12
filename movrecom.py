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
import scipy.sparse.linalg, json
import pandas as pd, numpy as np
import os, sys, re, csv

#d = "/mnt/3d1ece2f-6539-411b-bac2-589d57201626/home/burak/Downloads/ml-latest"
d = "/mnt/3d1ece2f-6539-411b-bac2-589d57201626/home/burak/Downloads/ml-25m"

def to_json():
    fin = d + "/ratings.csv"
    fout = d + "/ratings-json.csv"
    curruser = 0
    row_dict = {}
    fout = open(fout, "w")
    with open(fin) as csvfile:   
        rd = csv.reader(csvfile,delimiter=',')
        headers = {k: v for v, k in enumerate(next(rd))}
        for row in rd:
            if row[headers['userId']] != curruser:
                fout.write(str(curruser) + "|")
                fout.write(json.dumps(row_dict))
                fout.write("\n")
                fout.flush()
                curruser = row[headers['userId']]
                row_dict = {}       
            row_dict[int(row[headers['movieId']])] = float(row[headers['rating']])
    fout.close()


def simple_similarity():
    picks = pd.read_csv('movpicks.csv',index_col=0).to_dict('index')
    ratings = pd.read_csv(d + "/ratings.csv")
    utility_csr = csr_matrix((ratings.rating, (ratings.userId , ratings.movieId)))

    mov = pd.read_csv(d + "/movies.csv",index_col="title")['movieId'].to_dict()
    tst = np.zeros((1,utility_csr.shape[1]))
    for p in picks:
        if p in mov: tst[0,mov[p]] = float(picks[p]['rating']) 

    similarities = cosine_similarity(utility_csr, tst)

    close_people = np.argsort(similarities[:,0])
    movi = pd.read_csv(d + "/movies.csv",index_col="movieId")['title'].to_dict()
    genre = pd.read_csv(d + "/movies.csv",index_col="movieId")['genres'].to_dict()
    
    res = []
    # work from end of close people sort index (best to worst) get
    # their movies put on a list
    for idx in range(1,2000):
        ii,jj = utility_csr[close_people[-idx],:].nonzero()    
        for j in jj:
            r = utility_csr[close_people[-idx],:][0,j]
            n = movi[j]
            c = similarities[close_people[-idx],0]
            fres = re.findall('\((\d\d\d\d)\)', n)
            if len(fres)>0 and n not in picks  \
               and r >= 4.0 and 'Animation' not in genre[j]:
                year = int(fres[0])
                res.append([n, year, c])
    df = pd.DataFrame(res)
    df = df.sort_values([1,2],ascending=False)
    fout = '~/Downloads/movierecom.csv'
    df = df.drop_duplicates(0)
    df.to_csv(fout)
    print ('See ' + fout)


if __name__ == "__main__":  

    to_json()
    exit()
    
    if len(sys.argv) < 2:
        print ("Usage movrecom.py [normal]")
        exit()

    if sys.argv[1] == "normal":
        simple_similarity()
        exit()

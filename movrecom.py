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

def sim_prep():
    d = "."
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
    
def sim():
    import json, csv, pandas as pd, re
    import sys, numpy as np
    csv.field_size_limit(sys.maxsize)

    fin = d + "/ratings-json.csv"
    picks = pd.read_csv('movpicks.csv',index_col=0).to_dict('index')
    mov = pd.read_csv(d + "/movies.csv",index_col="title")['movieId'].to_dict()
    genre = pd.read_csv(d + "/movies.csv",index_col="movieId")['genres'].to_dict()
    mov_id_title = pd.read_csv(d + "/movies.csv",index_col="movieId")['title'].to_dict()
    picks_json = dict((mov[p],float(picks[p]['rating'])) for p in picks if p in mov)
    picks_norm = np.sqrt(sum(v**2 for v in picks_json.values()))
    res = []
    with open(fin) as csvfile:   
        rd = csv.reader(csvfile,delimiter='|')
        for i,row in enumerate(rd):
            jrow = json.loads(row[1])
            jrow_norm = np.sqrt(sum(v**2 for v in jrow.values()))
            dp = sum(jrow[key]*picks_json.get(int(key), 0) for key in jrow)
            dp = dp / (picks_norm*jrow_norm)
            res.append([row[0],dp])
            if i % 1e4 == 0: print (i,dp)

    df = pd.DataFrame(res).set_index(0)
    df = df.sort_values(by=1,ascending=False).head(400)
    df = df.to_dict()[1]

    recoms = []
    with open(fin) as csvfile:   
        rd = csv.reader(csvfile,delimiter='|')
        for i,row in enumerate(rd):
            jrow = json.loads(row[1])
            if str(row[0]) in df:
                for movid,rating in jrow.items():
                    fres = re.findall('\((\d\d\d\d)\)', mov_id_title[int(movid)])
                    if rating >= 4.5 and \
                       mov_id_title[int(movid)] not in picks and \
                       'Animation' not in genre[int(movid)] and \
                       len(fres)>0 and int(fres[0]) > 2005: \
                       recoms.append([mov_id_title[int(movid)],rating*df[row[0]]])

    df = pd.DataFrame(recoms)
    df = df.sort_values(1,ascending=False)
    df = df.drop_duplicates(0)
    df.to_csv("/opt/Downloads/movierecom2.csv",index=None,header=False)
       
if __name__ == "__main__":  
    
    if len(sys.argv) < 2:
        print ("Usage movrecom.py [sim]")
        exit()

    if sys.argv[1] == "sim":
        sim()

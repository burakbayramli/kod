'''
Create one-hot encoded dataframe that is ready to be joined in to
another df which has birthday on it
'''
from sha import sha
from sklearn.feature_extraction import DictVectorizer
from datetime import datetime
import pandas as pd, os
import mindmeld, numpy as np

def f(x):
    res = mindmeld.calculate(str(int(x['bday'])))
    for lew in res['lewi']: x['L'+str(lew)] = 1
    if res['chinese']: x['C'] = res['chinese']
    if res['spiller']: x['S'] = res['spiller']
    if res['millman']:
        x['M0'] = str(res['millman'][0])
        x['M1'] = str(res['millman'][1])
        x['M2'] = str(res['millman'][2])
        x['M3'] = str(res['millman'][3])
    return x

# get readings using birthday
df = pd.read_csv("./data/lewi.dat", sep=' ', names=['bday','rest','C','S','M0','M1','M2','M3'])
df['bday'] = df['bday'].map(lambda x: int(x))
df = df[df['bday'] > 19400101]

df = df[:100] ### FOR TESTING

for x in map(lambda x: 'L'+str(x),range(278)): df[x] = np.nan
df2 = df.apply(f, axis=1)
df2 = df2.drop('rest',axis=1)

# now encode 
ss = list(df2['S'].unique())
cs = list(df2['C'].unique())
for i in df2.index: df2.ix[i,'S'] = str(ss.index(df2.ix[i,'S']))
for i in df2.index: df2.ix[i,'C'] = str(cs.index(df2.ix[i,'C']))

df2['C'] = df2['C'].map(lambda x: str(x))
df2['S'] = df2['S'].map(lambda x: str(x))
df2['M0'] = df2['M0'].map(lambda x: str(x))
df2['M1'] = df2['M1'].map(lambda x: str(x))
df2['M2'] = df2['M2'].map(lambda x: str(x))
df2['M3'] = df2['M3'].map(lambda x: str(x))
    
df2.to_csv("/tmp/out.csv",sep=';',index=None)




```python
df = pd.read_csv('xch.csv',index_col=0)
c = df.corr()
print (c['EURTRY=X'])
```

```text
EURTRY=X    1.000000
TRYUSD=X   -0.805432
GBPTRY=X    0.998011
USDEUR=X    0.538231
USDGBP=X    0.632362
JPYUSD=X   -0.538154
GBPEUR=X   -0.264026
TRYCHF=X   -0.799924
CHFUSD=X    0.069238
CHFEUR=X    0.655910
TRYDKK=X   -0.845974
TRYJPY=X   -0.852979
CADUSD=X   -0.515936
DKKUSD=X   -0.526955
NOKUSD=X   -0.651586
SEKUSD=X   -0.652121
JPYEUR=X   -0.025502
USDAUD=X    0.601371
Name: EURTRY=X, dtype: float64
```


```python
from statsmodels.tsa.stattools import adfuller
import pandas as pd
import numpy.linalg as lin
pd.set_option('display.max_columns', None)

df = pd.read_csv('xch.csv',index_col=0)
df = df[df.index > '2016-01-01']
#df = df[['EURTRY=X','CADUSD=X','USDAUD=X']]
#df = df[['CADUSD=X','USDAUD=X']]
#df = df[['TRYUSD=X','USDAUD=X']]
#df = df[['EURTRY=X','GBPEUR=X']]
#df = df[['EURTRY=X','GBPTRY=X']]
#df = df[['EURTRY=X','GBPTRY=X']]

df = df[['EURTRY=X','GBPTRY=X']]; df = 1./df # TRYEUR=X','TRYGBP=X
df = df.interpolate()
Sigma = df.cov()
eval,evec = lin.eig(Sigma)
minidx = np.argmin(eval)
weights = np.round(evec[:,minidx],4)
print (weights)
df['yport0'] = np.dot(df, weights)
print (adfuller(df['yport0'])[1])
#df.yport0.plot()
#plt.savefig('tst1_01.png')
df.yport0.reset_index().plot()
plt.savefig('tst1_02.png')
```

```text
[-0.6368  0.771 ]
0.0023292158890772497
```



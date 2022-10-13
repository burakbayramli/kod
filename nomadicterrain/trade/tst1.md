

```python
from statsmodels.tsa.stattools import adfuller
import pandas as pd
import numpy.linalg as lin
pd.set_option('display.max_columns', None)

df = pd.read_csv('xch.csv',index_col=0)
df = df[df.index > '2016-01-01']
#df = df[['EURTRY=X','CADUSD=X','SEKUSD=X','USDAUD=X']] # 0.003
df = df[['EURTRY=X','CADUSD=X','USDAUD=X']]
df = df.interpolate()
Sigma = df.cov()
eval,evec = lin.eig(Sigma)
minidx = np.argmin(eval)
weights = evec[:,minidx]
print (weights)
df['yport0'] = np.dot(df, weights)
print (adfuller(df['yport0'])[1])
df.yport0.plot()
plt.savefig('tst1_01.png')
```

```text
[ 0.00317566 -0.9652821  -0.26119032]
0.0020185443188929595
```

```python
df.yport0.reset_index().plot()
plt.savefig('tst1_02.png')
```



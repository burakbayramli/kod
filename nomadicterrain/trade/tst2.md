


```python
import pandas as pd
pd.set_option('display.max_columns', None)
y1 = np.array([1 for i in range (10)])
y2 = np.array([0.2*i for i in range(11,20)])
y3 = np.array([1 for i in range (21,30)])
y = np.hstack((y1,y2,y3))
df = pd.DataFrame(y)
df.columns = ['y']
df['y'].plot(ylim=[0,5])
#print (df)
plt.savefig('tst2_01.png')
```

```python
df['positions'] = 1.0
ret = df['positions'] * df['y'].pct_change()*100.0
res = np.cumprod(1+ret)-1
print (res)
#plt.savefig('tst2_02.png')
```

```text
0              NaN
1     0.000000e+00
2     0.000000e+00
3     0.000000e+00
4     0.000000e+00
5     0.000000e+00
6     0.000000e+00
7     0.000000e+00
8     0.000000e+00
9     0.000000e+00
10    1.200000e+02
11    1.220000e+03
12    1.139500e+04
13    9.905654e+04
14    8.066104e+05
15    6.184020e+06
16    4.483415e+07
17    3.085644e+08
18    2.022811e+09
19   -1.470264e+11
20   -1.470264e+11
21   -1.470264e+11
22   -1.470264e+11
23   -1.470264e+11
24   -1.470264e+11
25   -1.470264e+11
26   -1.470264e+11
27   -1.470264e+11
dtype: float64
```









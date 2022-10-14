


```python
import pandas as pd
df = pd.read_csv('simple1.csv')
print (df)
df['positions'] = 1.0
df.loc[df.index > 15,'positions'] = 0
ret = df['positions'] * df['y'].pct_change()*100.0
ret = df.positions.shift(1) * (df['y'] - df['y'].shift(1))  / df['y'].shift(1)
res = np.cumprod(1+ret)-1
print (res)
res.plot()
plt.savefig('tst2_02.png')
```

```text
      y
0   1.0
1   1.0
2   1.0
3   1.0
4   1.0
5   1.0
6   1.0
7   1.0
8   1.0
9   1.0
10  2.2
11  2.4
12  2.6
13  2.8
14  3.0
15  3.2
16  3.4
17  3.6
18  3.8
19  1.0
20  1.0
21  1.0
22  1.0
23  1.0
24  1.0
25  1.0
26  1.0
27  1.0
0     NaN
1     0.0
2     0.0
3     0.0
4     0.0
5     0.0
6     0.0
7     0.0
8     0.0
9     0.0
10    1.2
11    1.4
12    1.6
13    1.8
14    2.0
15    2.2
16    2.4
17    2.4
18    2.4
19    2.4
20    2.4
21    2.4
22    2.4
23    2.4
24    2.4
25    2.4
26    2.4
27    2.4
dtype: float64
```









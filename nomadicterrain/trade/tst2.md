


```python
import pandas as pd
y1 = pd.Series([1 for i in range (10)])
y2 = pd.Series([0.2*i for i in range(11,20)])
y3 = pd.Series([1 for i in range (21,30)])
df = pd.concat([y1,y2,y3],axis=0).reset_index()
df[0].plot(ylim=[0,5])
plt.savefig('tst2_01.png')
```













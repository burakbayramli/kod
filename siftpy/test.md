
```python
import siftpy1
from PIL import Image
df1 = siftpy1.sift("crans_1_small.pgm",threshold=10.0)
print len(df1)
```

```text
2939
```

```python
import pandas as pd
im=Image.open("crans_2_small.jpg")
df2.plot(kind='scatter',x=0,y=1)
plt.hold(True)
plt.imshow(im)
plt.savefig('test_02.png')
```


```python
df2 = siftpy1.sift("crans_2_small.pgm",threshold=10.0)
```

```python
import pandas as pd
im=Image.open("crans_2_small.jpg")
df2.plot(kind='scatter',x=0,y=1)
plt.hold(True)
plt.imshow(im)
plt.savefig('test_02.png')
```

```python
df1.to_csv("/tmp/out1.csv",index=None)
df2.to_csv("/tmp/out2.csv",index=None)
```

```python
import pandas as pd
df1 = pd.read_csv('/tmp/out1.csv')
df2 = pd.read_csv('/tmp/out2.csv')
```

```python
res = siftpy1.match_twosided(df1,df2)
```

```python
print len(res)
```

```text
2939
```


```python
df1['match'] = res
df1.to_csv('/tmp/out1match.csv')
```




















































```python
from scipy import io as spio
import pandas as pd
a = spio.loadmat('ip11.mat')['A']
print a
keypoints1 = spio.loadmat('mp1.mat')['A']
print keypoints1.shape
print keypoints1
keypoints2 = spio.loadmat('mp2.mat')['A']
print keypoints2.shape
print keypoints2
```

```text
[[   7]
 [   8]
 [   9]
 ..., 
 [7794]
 [7796]
 [7811]]
(1246, 2)
[[ 1696.3605957   1141.05566406]
 [ 1268.25695801  1093.1270752 ]
 [  920.66223145   648.5369873 ]
 ..., 
 [  824.76031494  1038.06530762]
 [ 1381.35375977   690.07543945]
 [  588.68914795   626.89837646]]
(1246, 2)
[[ 1474.75695801  1217.9173584 ]
 [ 1341.09509277  1139.43713379]
 [  956.71398926   644.62701416]
 ..., 
 [  856.44952393  1079.10559082]
 [ 1480.06164551   670.22161865]
 [  601.61724854   628.72119141]]
```

```python
import itertools
lines = []
for (x,y) in itertools.izip(keypoints1,keypoints2):
    lines.append([tuple(x),tuple(y)])
```

```python
df1 = pd.DataFrame(keypoints1)
df2 = pd.DataFrame(keypoints2)
```

```python
from PIL import Image

f = plt.figure()
ax = f.add_subplot(111)
im = Image.open('/home/burak/Downloads/pcv_data/data/alcatraz1.pgm')
plt.imshow(im,cmap = plt.get_cmap('gray'))
plt.hold(True)
df1.plot(kind='scatter',x=0,y=1,ax=ax,marker='.',color='red')
plt.hold(True)
df2.plot(kind='scatter',x=0,y=1,ax=ax,marker='+',color='green')
plt.hold(True)
from matplotlib import collections  as mc
lc = mc.LineCollection(lines,color='yellow')
ax.add_collection(lc)
plt.savefig('/tmp/mvg_06.png')
```









































































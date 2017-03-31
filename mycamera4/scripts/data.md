
## Veri İşlemek

Telefon tarafından toplanan verileri nasıl kullanırız? 

```python
import pandas as pd

dir = "./data/mitte4/"
data = np.fromfile(dir + "cam.bin", dtype=np.uint8)
df = pd.read_csv(dir + "sizes.txt",header=None)
df['cum'] = df.cumsum()
df['cum2'] = df.cum.shift(-1)
df.columns = ['x','fr','to']
```

Herhangi bir video karesini çekip çıkarmak


```python
import io
from PIL import Image
frame = 30
arr = data[int(df.ix[frame]['fr']) : int(df.ix[frame]['to'])]
im = Image.open(io.BytesIO(arr))
im.save('out1.png')
```

![](out1.png)

Herhangi zaman anında gittiğimiz yön,

```python
dforient = pd.read_csv(dir + "orientations.txt",header=None,sep=' ')
frame = 50
print str(dforient.ix[frame][1])[:6] + " " + \
      str(dforient.ix[frame][2])[:6] + " " + \
      str(dforient.ix[frame][3])[:6] + " ",
```

```text
334.46 0.8803 84.372 
```

GPS

```python
dfgps = pd.read_csv(dir + "gps.txt",header=None,sep=",",\
                    names=['lat','lon','speed','acc','alt'])
print str(dfgps.ix[frame][0])[:6] + " " + \
      str(dfgps.ix[frame][1])[:6] + " " + \
      str(dfgps.ix[frame][2])[:6] + "   " 
```

```text
52.511 13.390 0.6519   
```

Dört köşesi üzerinden belirtilen bir dörtgeni ekrana basmak.

```python
import util
im = util.get_frame(dir, 105)
quad = np.array([[100,0],[143,100.],[202,100],[224,0]])
h = np.array(im).shape[0]
util.plot_quad(quad, h, 'y')
plt.imshow(im)
plt.savefig('out2.png')
```

![](out2.png)


Bazen bu dörtgenin içindeki pikselleri bulmak gerekebilir. Fakat tüm
pikselleri de bulmak külfetli olacaktır, en iyisi resim kordinatları
içinde tanımlı birörnek (uniform) bir dağılımdan sayılar (kordinatlar)
örneklemek, bu kordinatlardan dörtgen içine düşenleri bulmak, ve bu
daha az sayıdaki kordinatları kullanmak.

```python
np.random.seed(1)
random_points = np.random.uniform(0, 320, (1000, 2)).astype(np.int)
random_points = random_points[random_points[:,1] < 240]
mask = np.array([util.inside_quad(quad, p)[0] for p in random_points])
plt.plot(random_points[mask][:,0], h-random_points[mask][:,1], 'r.')
util.plot_quad(quad, h, 'y')
plt.imshow(im)
plt.savefig('out3.png')
```

![](out3.png)

Dörtgen içindeki kordinatların renk değerlerini alıp tüm bu değerlerin
histogramını hesaplayabiliriz.


```python
bins=(8, 8, 8)
nim = np.array(im)
nim_quad = nim[random_points[mask][:,1],random_points[mask][:,0]]
H, edges = np.histogramdd(nim_quad, bins=bins, normed=True, range=[(0,255),(0,255),(0,255)])
print 'H', H.shape, 'edges', len(edges)
```

```text
H (8, 8, 8) edges 3
```

Bu histogram çok boyutlu, yani üç boyutlu HSV verisi üzerinde (8,8,8)
kutuları yarattık, yani elimizde şimdi 8*8*8 tane kutu var. Bildiğimiz
gibi bir histogram bir olasılıksal dağılımı ayrıksal olarak temsil
eder. O zaman bu dağılıma herhangi bir HSV değerinin ne kadar olası olduğunu
"sorabiliriz". 

```python
def eval(x, H, edges):
    i=np.argmax(x[0]<edges[0])
    j=np.argmax(x[1]<edges[1])
    k=np.argmax(x[2]<edges[2])
    return H[i-1,j-1,k-1]

print eval([156,17,191], H, edges)
```

```text
1.23512073034e-06
```



Yardımcı kodlar [şurada](util.py) bulunabilir.












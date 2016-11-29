
MyCamera
========

Kamera resimleri, yon algilayicisi (orientation sensor), GPS
degerlerini belli araliklarla kaydeder, eger GPS okunduysa olunan
yerin haritasini verir. Haritalar bir zip dosyasi icinde mevcut, ornek
harita olarak

https://dl.dropboxusercontent.com/u/1570604/data/istanbul.zip

Bu dosyayi SDCARD/Bass/

dizini altina kopyalayinca is tamamlanir. App baslatilinca hangi
harita secildigi de girilmeli. Kayit islemi bitince gerekli dosyalar
SDCARD/Bass altinda. Dosyalari okumak icin Python kodu altta.


English
========

Example of how to use Android camera, recording of orientation sensor
and GPS. The data file will be under SDCARD/Bass folder, which can be
taken to notebook for analysis. Data can be analyzed with

```
import pandas as pd

data = np.fromfile("cam.txt", dtype=np.uint8)
df = pd.read_csv("sizes.txt",header=None)
df['cum'] = df.cumsum()
df['cum2'] = df.cum.shift(-1)
df.columns = ['x','fr','to']
```

for textual data, for cam images

```
import io
from PIL import Image
frame = 8
arr = data[int(df.ix[frame]['fr']) : int(df.ix[frame]['to'])]
im = Image.open(io.BytesIO(arr))
im.save('out.png')
```

The camera part is based on

http://ibuzzlog.blogspot.tw/2012/08/how-to-use-camera-in-android.html


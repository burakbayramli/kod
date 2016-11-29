
MyCamera
========

Kamera resimleri, yon algilayicisi (orientation sensor), GPS
degerlerini belli araliklarla kaydeder. Eger GPS okunduysa map
dugmesina basilarak o anda olunan yerin haritasi alinabilir. Haritalar
bir zip dosyasi icinde, ornek harita olarak

https://dl.dropboxusercontent.com/u/1570604/data/istanbul.zip

Bu dosyayi SDCARD/Bass/ dizini altina kopyalamak yeterli, bir de app
baslatilinca hangi harita secildigi de girilmeli.

Dosya icindeki harita parcalari png dosyalari olarak kayitli, hangi
GPS kordinatinin haritasi olduklari dosya isminde kodlanmis halde,
kordinat haritanin orta noktasidir.

Algilayici kayitlama uygulama bittiginde yapilir, kayit SDCARD/Bass
altina yazilir. Dosyalari okumak icin ornek Python kodu alttadir.

Android / Java teknigi olarak faydali olabilecek bazi kod bolumleri:

- ZIP icinden dosya okumak: Tum haritalar zip icinde, zip icine bakip
  oradaki dosya isimlerini almak, sonra istenilen tek dosyayi okuma
  teknigi var.

- Duzenli Ifadeler (Regex): Harita orta noktasi kordinati harita dosya
  isminde kodlu oldugu icin kordinatin geri alinmasi dosya ismini
  regex ile tarayip icinden GPS enlem, boylam verisini almakla
  oluyor. Dosya isminde kordinat kodlama basitlik amacli yapildi, eger
  ayri bir metin dosyasinda kayit olsaydi idare etmek zorlasirdi. Ana
  amac her zaman kod (veri) idaresinde kolaylik.

English
========

Example of how to use Android camera, recording of orientation sensor,
GPS, and camera frames. We skip some frames for performance / storage
reasons, and the unit of storage for all recording is the
frame. Whenever a frame is recorded, orientation, GPS will be recorded
along with it.

The data files are dumped under SDCARD/Bass folder, which can be taken
transferred to notebook for analysis, etc. Data can be analyzed with,

```
import pandas as pd

data = np.fromfile("cam.txt", dtype=np.uint8)
df = pd.read_csv("sizes.txt",header=None)
df['cum'] = df.cumsum()
df['cum2'] = df.cum.shift(-1)
df.columns = ['x','fr','to']
```

for textual data, and for cam images

```
import io
from PIL import Image
frame = 8
arr = data[int(df.ix[frame]['fr']) : int(df.ix[frame]['to'])]
im = Image.open(io.BytesIO(arr))
im.save('out.png')
```

The camera, preview code is based on

http://ibuzzlog.blogspot.tw/2012/08/how-to-use-camera-in-android.html


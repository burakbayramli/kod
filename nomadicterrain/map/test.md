

```python
import json, os
params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())
import plot_map
pts = [[51.2130605,4.4174822]]
zfile,scale = params['mapzip']['terrain']
print (scale)
plot_map.plot(pts,'out.png',zfile=zfile,scale=scale)
```

```text
[2900, -4600]
[[51.2130605, 4.4174822]]
europe2/europe2_map_51_14678787341772_4_321983594936709.png
[51.14678787341772 4.321983594936709]
```





```python
import os, glob, re, zipfile
zfile = '/home/burak/Downloads/campdata/europe2.zip'
with zipfile.ZipFile(zfile, 'r') as z:
    res = z.namelist()
    print (len(res))
    
```

```text
6401
```







```python
import get_map

get_map.get_map(48.788836,2.898861, ".", zoom=11)
```

```text
http://maps.googleapis.com/maps/api/staticmap?center=48.788836,2.898861&size=800x800&maptype=terrain&zoom=11&key=AIzaSyCxQopw-CIBAdyUrhYk18LC_qurTjUWWlE

Out[1]: True
```








```python
import geopy
import geopy.distance
# baslangic noktasi
start = [48.853, 2.349]
print (start)
start = geopy.Point(start[0], start[1])
# mesafe 1 km
d = geopy.distance.VincentyDistance(kilometers = 1)
# derece olarak adim atilacak yon, 0 derece kuzey
print 
reached = d.destination(point=start, bearing=180)
print ([reached.latitude, reached.longitude])
```

```text
[48.853, 2.349]
[48.84400774427651, 2.349]
```

```python
print (help(d.destination))
print (np.deg2rad(180))
```

```text
Help on method destination in module geopy.distance:

destination(point, bearing, distance=None) method of geopy.distance.vincenty instance
    TODO docs.

None
3.141592653589793
```




```python
from shapely.geometry import Polygon
pts = [[51.238689, 4.406747],[51.232246, 4.444266],[51.251485,4.472641],[51.265894, 4.452429]]
p = Polygon(pts)
print (p.centroid.x)
print (p.centroid.y)
```

```text
51.24722013653824
4.442773906875942
```

```python
pts2 = np.array(pts)
print (pts2.mean(axis=0))
```

```text
[51.2470785   4.44402075]
```





```python
import plot_map
#pts = np.array([[42.657889, 18.087956],[42.674881, 18.144730]])
#pts = np.array([[51.218343232,4.404985494]])
pts = np.array([[51.21306,4.41175286]])
plot_map.plot(pts,'out.png')
```

```text
europe2/europe2_map_51_14678787341772_4_321983594936709.png
[51.14678787341772 4.321983594936709]
```

```python
import geopy.distance
import pandas as pd
df = pd.read_csv('/home/burak/Downloads/eu-camping.csv')
center = (42.657889, 18.087956)
dists = df.apply(lambda x: geopy.distance.vincenty((x['Latitude'],x['Longitude']),center).km, axis=1)
dists = dists.sort_values(ascending=True)
print (dists.index[0])
print (df.ix[dists.index[0]][['Latitude','Longitude']]   )

```

```text
290
Latitude     39.9847
Longitude    21.5024
Name: 290, dtype: object
```



```python
pts = np.array([[39.9847,21.5024]])
plot_map.plot(pts,'out2.png')
```

```text
europe2/europe2_map_39_916099189873414_21_637753430379746.png
[39.916099189873414 21.637753430379746]
```











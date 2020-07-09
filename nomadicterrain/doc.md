
```python
3000 * 70
```

```text
Out[1]: 210000
```

```python
import json, os
import plot_map
#pts = [[40.962166, 29.101865]]
pts = [[40.965860, 29.126493]]
zfile,scale = "/tmp/turkey1.zip",(12000,-12000)
#zfile,scale = params['mapzip']['terrain']
#zfile,scale = params['mapzip']['istanbul']
#zfile,scale = params['mapzip']['world2']
plot_map.plot(pts,'out.png',zfile=zfile,scale=scale)
```

```text
/tmp/turkey1.zip
40_963305867350066_29_11376953125.jpg
[40.963305867350066 29.11376953125]
```


```python
import get_map
#get_map.get_map(48.788836,2.898861, ".", zoom=11)
get_map.get_map(36.525643, 32.090421, ".", zoom=12)
```

```text
http://maps.googleapis.com/maps/api/staticmap?center=36.525643,32.090421&size=800x800&scale=2&maptype=terrain&zoom=12&key=AIzaSyCxQopw-CIBAdyUrhYk18LC_qurTjUWWlE

Out[1]: True
```











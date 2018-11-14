

```python
import json, os
params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())
import plot_map
#pts = [[48.306219, 7.452300]] # near mountain
#pts = [[51.2130605,4.4174822]] # antwerp
#pts = [[40.987659,29.036428],[40.992186,29.039228]] # tr
pts = [[36.551907, 32.193444]] # alanya park
#pts = [[52.510811, 13.370794]] # pots
#zfile,scale = params['mapzip']['terrain']
#zfile,scale = params['mapzip']['istanbul']
#zfile,scale = params['mapzip']['world2']
zfile = '/home/burak/Downloads/campdata/turkey1.zip'
scale = [1450,-2400]
print (scale)
plot_map.plot(pts,'out.png',zfile=zfile,scale=scale)
```

```text
[1450, -2400]
[[36.551907, 32.193444]]
turkey/turkey_map_36_58421160759494_32_1309096835443.png
[36.58421160759494 32.1309096835443]
```


```python
import get_map
get_map.get_map(48.788836,2.898861, ".", zoom=11)
```

```text
http://maps.googleapis.com/maps/api/staticmap?center=48.788836,2.898861&size=800x800&maptype=terrain&zoom=11&key=AIzaSyCxQopw-CIBAdyUrhYk18LC_qurTjUWWlE

Out[1]: True
```











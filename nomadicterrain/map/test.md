

```python
import json, os
params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())
import plot_map
#pts = [[48.306219, 7.452300]] # near mountain
#pts = [[51.2130605,4.4174822]] # antwerp
#pts = [[40.987659,29.036428],[40.992186,29.039228]] # tr
#pts = [[36.551907, 32.193444]] # alanya park
#pts = [[52.510811, 13.370794]] # pots
pts = [[36.557532, 32.064841]] # alanya 
#zfile,scale = params['mapzip']['terrain']
#zfile,scale = params['mapzip']['istanbul']
#zfile,scale = params['mapzip']['world2']
zfile = '/home/burak/Downloads/campdata/turkey3.zip'
scale = [2900,-3500]
print (scale)
plot_map.plot(pts,'out.png',zfile=zfile,scale=scale)
```

```text
[2900, -3500]
turkey3/turkey3_map_36_52373806329114_32_05089974683544.png
[36.52373806329114 32.05089974683544]
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











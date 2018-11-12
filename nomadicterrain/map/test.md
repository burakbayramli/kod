

```python
import json, os
params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())
import plot_map
pts = [[48.306219, 7.452300]] # near mountain
#pts = [[51.2130605,4.4174822]] # antwerp
#pts = [[40.987659,29.036428],[40.992186,29.039228]] # tr
#pts = [[52.510811, 13.370794]] # pots
zfile,scale = params['mapzip']['terrain']
#zfile,scale = params['mapzip']['istanbul']
#zfile,scale = params['mapzip']['world2']
print (scale)
plot_map.plot(pts,'out.png',zfile=zfile,scale=scale)
```

```text
[1450, -2400]
[[48.306219, 7.4523]]
europe3/europe3_map_48_263232670886076_7_377707683544304.png
[48.263232670886076 7.377707683544304]
```


```python
import get_map
get_map.get_map(48.788836,2.898861, ".", zoom=11)
```

```text
http://maps.googleapis.com/maps/api/staticmap?center=48.788836,2.898861&size=800x800&maptype=terrain&zoom=11&key=AIzaSyCxQopw-CIBAdyUrhYk18LC_qurTjUWWlE

Out[1]: True
```











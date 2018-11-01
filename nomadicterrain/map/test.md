

```python
import uuid
print(uuid.uuid4())
```

```text
71f0b7d7-ae78-442d-8019-c0c55ee4932a
```










```python
import geopy.distance

dist = geopy.distance.vincenty((51.238689, 4.406747),(51.232246, 4.444266))

print (dist)
print (dist.km)
```

```text
2.716664570050969 km
2.716664570050969
```









```python
from shapely.geometry import Polygon
pts = [[51.238689, 4.406747],[51.232246, 4.444266],[51.251485,4.472641],[51.265894, 4.452429]]
p = Polygon(pts)
print (p.centroid.x)
print (p.centroid.y)
```

```text
<class 'shapely.geometry.point.Point'>
51.24722013653824
4.442773906875942
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











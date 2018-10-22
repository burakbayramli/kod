

```python
import plot_map

#pts = np.array([[46.718425, 9.975458]])
pts = np.array([[42.657889, 18.087956],[42.674881, 18.144730]])
plot_map.plot(pts,'out.png')
```

```text
1704
europe2/europe2_map_42_64788832911392_18_242504443037973.png
[42.64788832911392 18.242504443037973]
```

```python

import geopy.distance
import pandas as pd
df = pd.read_csv('/home/burak/Downloads/eu-camping.csv')
#print (df[['Latitude','Longitude']])
#print (df.columns)
center = (42.657889, 18.087956)
dists = df.apply(lambda x: geopy.distance.vincenty((x['Latitude'],x['Longitude']),center).km, axis=1)
dists = dists.sort_values(ascending=True)
print (dists.index[0])
#sorted = dists.argsort()
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











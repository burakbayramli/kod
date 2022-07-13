
```python
import pandas as pd

picks = pd.read_csv('movpicks.csv',index_col=0).to_dict('index')
print (picks['Black Panther (2017)']['rating'])
for p in picks: print (picks[p])
```

```text
4
```






```python
from sklearn.metrics.pairwise import cosine_similarity
from scipy.sparse import csr_matrix
import scipy.sparse.linalg, json
import pandas as pd, numpy as np
import os, sys, re

d = "/mnt/3d1ece2f-6539-411b-bac2-589d57201626/home/burak/Downloads/ml-latest"

#picks = json.loads(open("movpicks.json").read())
picks = pd.read_csv('movpicks.csv',index_col=0).to_dict('index')

skips = json.loads(open("movskips.json").read())

ratings = pd.read_csv(d + "/ratings.csv")
utility_csr = csr_matrix((ratings.rating, (ratings.userId , ratings.movieId)))

mov = pd.read_csv(d + "/movies.csv",index_col="title")['movieId'].to_dict()
tst = np.zeros((1,utility_csr.shape[1]))
for p in picks: tst[0,mov[p]] = float(picks[p]['rating'])
similarities = cosine_similarity(utility_csr, tst)
```

```python
# user - movie
print (utility_csr.shape)
print (similarities)
print (similarities.shape)
print (similarities.sum())
```

```text
(283229, 193887)
[[0.        ]
 [0.        ]
 [0.        ]
 ...
 [0.        ]
 [0.        ]
 [0.05312242]]
(283229, 1)
7354.729566013386
```

```python
closest_ppl = np.argsort(similarities[:,0])
print (closest_ppl)
print (similarities[closest_ppl[-1],0])
```

```text
[     0 246006 246007 ...  49202  40864   2917]
0.5404320471492292
```















```python
import pickle

features = pickle.load(open("/home/burak/Downloads/features.pkl","rb"))
labels = pickle.load(open("/home/burak/Downloads/labels.pkl","rb"))
label_dict = pickle.load(open("/home/burak/Downloads/label_dict.pkl","rb"))
label_rev_dict = dict((label_dict[k],k) for k in label_dict)

from sklearn.model_selection import train_test_split
x_train, x_test, y_train, y_test = train_test_split(features, labels, random_state=42, test_size=0.05)
```


```python
print (len(labels))
print (len(x_train))
print (len(y_train))
print (y_train[:20])
```

```text
11952
11354
11354
[142, 196, 209, 66, 7, 51, 26, 136, 256, 225, 185, 136, 16, 166, 203, 93, 233, 156, 168, 43]
```


```python
from sklearn.linear_model import SGDClassifier
clf = SGDClassifier(loss="log", penalty="l2", max_iter=10)
clf.fit(x_train, y_train)
```

```text
Out[1]: 
SGDClassifier(alpha=0.0001, average=False, class_weight=None,
       early_stopping=False, epsilon=0.1, eta0=0.0, fit_intercept=True,
       l1_ratio=0.15, learning_rate='optimal', loss='log', max_iter=10,
       n_iter=None, n_iter_no_change=5, n_jobs=None, penalty='l2',
       power_t=0.5, random_state=None, shuffle=True, tol=None,
       validation_fraction=0.1, verbose=0, warm_start=False)
```

```python
import pickle
pickle.dump(clf, open("/home/burak/Downloads/lr-plants.pkl", 'wb'))
```

```python
import pickle
clf2 = pickle.load(open("/home/burak/Downloads/lr-plants.pkl", 'rb'))
```

```python
rank_1 = 0
rank_5 = 0
# loop over test data
for (label, features) in zip(y_test, x_test):
  # predict the probability of each class label and
  # take the top-5 class labels
  predictions = clf2.predict_proba(np.atleast_2d(features))[0]
  predictions = np.argsort(predictions)[::-1][:5]

  # rank-1 prediction increment
  if label == predictions[0]:
    rank_1 += 1

  # rank-5 prediction increment
  if label in predictions:
    rank_5 += 1

# convert accuracies to percentages
rank_1 = (rank_1 / float(len(y_test))) * 100
rank_5 = (rank_5 / float(len(y_test))) * 100

print (rank_1)
print (rank_5)
```

```text
10.200668896321071
27.09030100334448
```


```python
y_train_arr = np.array(y_train)
x_train_arr = np.array(x_train)
print (len(x_train), len(y_train))
print (len(y_train_arr[y_train_arr==0]))
print (label_rev_dict[0])
data_0 = x_train_arr[y_train_arr==0]
data_1 = x_train_arr[y_train_arr==1]
```

```text
11354 11354
45
Achillea millefolium
```

```python
from scipy.spatial.distance import cosine
#for x in x_train_arr:
print (cosine(x_train_arr[0],x_train_arr[1]))
print (cosine(x_train_arr[0],x_train_arr[2]))
print (cosine(x_train_arr[0],x_train_arr[3]))
```

```text
0.01041942834854126
0.0328635573387146
0.016966700553894043
```

```python
from scipy.spatial.distance import euclidean
def cluster(X,):
    examplars = []
    for x in X:
        if len(examplars)==0: examplars.append(x)
        dists = np.array([euclidean(x,e) for e in examplars])
        if not np.any(dists < 25): examplars.append(x)
    clusters = []
    for x in X:
        dists = np.array([euclidean(x,e) for e in examplars])
        clusters.append(np.argmin(dists))

    return examplars, clusters

d = data_0
print (np.mean(d))
print (np.std(d))
examplars, clusters = cluster(d)
print (clusters)
print (len(examplars))
```

```text
1.1155998
1.0285106
[0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 1, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 0, 0, 1, 0]
2
```





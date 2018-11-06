
```python
import pickle

features = pickle.load(open("/home/burak/Downloads/features.pkl","rb"))
labels = pickle.load(open("/home/burak/Downloads/labels.pkl","rb"))
label_dict = pickle.load(open("/home/burak/Downloads/label_dict.pkl","rb"))

from sklearn.model_selection import train_test_split
x_train, x_test, y_train, y_test = train_test_split(features, labels, test_size=0.05)
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
[76, 253, 102, 238, 96, 196, 108, 174, 52, 160, 5, 262, 239, 1, 39, 221, 180, 263, 47, 193]
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
  predictions = clf.predict_proba(np.atleast_2d(features))[0]
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
12.37458193979933
28.762541806020064
```














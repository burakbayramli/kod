
```python
import pickle

features = pickle.load(open("/home/burak/Downloads/features_new.pkl","rb"))
labels = pickle.load(open("/home/burak/Downloads/labels_new.pkl","rb"))
label_dict = pickle.load(open("/home/burak/Downloads/labels_dict_new.pkl","rb"))
label_rev_dict = dict((label_dict[k],k) for k in label_dict)

from sklearn.model_selection import train_test_split
x_train, x_test, y_train, y_test = train_test_split(features, labels, random_state=42, test_size=0.10)

y_train_arr = np.array(y_train)
x_train_arr = np.array(x_train)
print (x_train_arr.shape)
print (len(x_test))
```

```text
(10431, 4096)
1160
```



```python
from sklearn.linear_model import SGDClassifier
clf = SGDClassifier(loss="log", penalty="l2", max_iter=5)
clf.fit(x_train, y_train)
```

```text
Out[1]: 
SGDClassifier(alpha=0.0001, average=False, class_weight=None,
       early_stopping=False, epsilon=0.1, eta0=0.0, fit_intercept=True,
       l1_ratio=0.15, learning_rate='optimal', loss='log', max_iter=5,
       n_iter=None, n_iter_no_change=5, n_jobs=None, penalty='l2',
       power_t=0.5, random_state=None, shuffle=True, tol=None,
       validation_fraction=0.1, verbose=0, warm_start=False)
```

```python
import pickle
pickle.dump(clf, open("/home/burak/Downloads/lr-plants-new.pkl", 'wb'))
```

```python
import pickle
clf2 = pickle.load(open("/home/burak/Downloads/lr-plants-new.pkl", 'rb'))
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
11.206896551724139
22.32758620689655
```


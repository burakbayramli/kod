from scipy.spatial.distance import euclidean
import pickle
import pandas as pd
import numpy as np

features = pickle.load(open("/home/burak/Downloads/features.pkl","rb"))
labels = pickle.load(open("/home/burak/Downloads/labels.pkl","rb"))
label_dict = pickle.load(open("/home/burak/Downloads/label_dict.pkl","rb"))
label_rev_dict = dict((label_dict[k],k) for k in label_dict)

x_train = features
y_train = labels

y_train_arr = np.array(y_train)
x_train_arr = np.array(x_train)
print (x_train_arr.shape)

def cluster(X,threshold=30):
    examplars = []
    for x in X:
        if len(examplars)==0: examplars.append(x)
        dists = np.array([euclidean(x,e) for e in examplars])
        if not np.any(dists < threshold): examplars.append(x)
    clusters = []
    for x in X:
        dists = np.array([euclidean(x,e) for e in examplars])
        clusters.append(np.argmin(dists))

    return examplars, clusters


x_train_new = []
y_train_new = []
label_dict_new = {}
for label in label_rev_dict.keys():
    print ("label", label)
    dataset_for_p = x_train_arr[y_train_arr==label]
    examp, clusters = cluster(dataset_for_p)
    print ('------------------------')
    print ("original size", len(dataset_for_p))
    #print (clusters)
    print ("unique clusters", np.unique(clusters))
    clusters_arr = np.array(clusters)
    for c in np.unique(clusters):
        if len(clusters_arr[clusters_arr==c]) > (len(clusters_arr)/4.):
           #print (len(clusters_arr)/4.)
           tmp = dataset_for_p[clusters==c]
           print (len(tmp))
           label_new = len(label_dict_new)
           label_dict_new[label_rev_dict[label] + str(c)] = label_new
           for t in tmp:
                x_train_new.append(t)
                y_train_new.append(label_new)

    
print ("orig", len(x_train))
print ("new", len(x_train_new))

pickle.dump(x_train_new, open("/home/burak/Downloads/features_new.pkl", 'wb'))
pickle.dump(y_train_new, open("/home/burak/Downloads/labels_new.pkl", 'wb'))
pickle.dump(label_dict_new, open("/home/burak/Downloads/labels_dict_new.pkl", 'wb'))

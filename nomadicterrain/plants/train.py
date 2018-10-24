# %load_ext autoreload
# %autoreload 2
import pandas as pd, re
import numpy as np
import os; os.environ['KERAS_BACKEND'] = 'theano'
import keras
from keras.datasets import mnist
from keras.models import Sequential
from keras.layers import Dense, Dropout
from keras.optimizers import RMSprop
from keras.preprocessing import image
from keras.applications.imagenet_utils import decode_predictions
from keras.applications.vgg16 import VGG16
from keras.applications.resnet50 import preprocess_input
import random
import zipfile
     
import pandas as pd
df = pd.read_csv('meta.csv',sep='\t',index_col=1)

with zipfile.ZipFile('/home/burak/Downloads/edible-wild-plants/datasets.zip', 'r') as z:
     im_files = list(z.namelist())
     im_files = [x for x in im_files if ".jpg" in x]

idxs = range(0, len(im_files))

with zipfile.ZipFile('/home/burak/Downloads/edible-wild-plants/datasets.zip', 'r') as z:
     def get_batch(batch_size = 30):
          X = np.zeros((batch_size,224,224,3))
          y = np.zeros((batch_size, 62))
          ridxs = random.sample(idxs, batch_size)
          for i in range(batch_size):
               f = im_files[ridxs[i]]
               regex = "dataset.*?/(.*?)/.*?\.jp"
               label_str = re.findall(regex, f)[0]
               print (label_str)
               label_idx = df.ix[label_str].Nr
               im = image.load_img(z.open(f), target_size=(224, 224))
               im = image.img_to_array(im)
               X[i, :] = im
               y[i,label_idx] = 1.0
          return X, y

     X, y = get_batch()
     print (X)
     print (y)
     exit()
     
     for i in range(100):
          X, y = get_batch()
          print (X.shape)
          print (y.shape)

#for e in range(epochs):
#    print('Epoch', e)
#    batches = 0
#    for x_batch, y_batch in datagen.flow(x_train, y_train, batch_size=32):
#        model.fit(x_batch, y_batch)

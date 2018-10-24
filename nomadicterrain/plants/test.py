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
     im_files = [x for x in im_files if ".jpg" in x and "user_images" not in x]

idxs = range(0, len(im_files))

with zipfile.ZipFile('/home/burak/Downloads/edible-wild-plants/datasets.zip', 'r') as z:
     def get_batch(batch_size = 30):
          X = np.zeros((batch_size,224,224,3))
          y = np.zeros((batch_size, 62))
          ridxs = random.sample(idxs, batch_size)
          for i in range(batch_size):
               f = im_files[ridxs[i]]
               print (f)
               regex = "dataset.*?/(.*?)/.*?\.jp"
               res = re.findall(regex, f)
               print (res)
               label_str = res[0]
               label_idx = int(df.ix[label_str].Nr)-1
               im = image.load_img(z.open(f), target_size=(224, 224))
               im = image.img_to_array(im)
               X[i, :] = im
               y[i,label_idx] = 1.0
          return X, y
     
     for i in range(100):
          X, y = get_batch()


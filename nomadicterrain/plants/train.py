# Google Collab
# %load_ext autoreload
# %autoreload 2
# from google.colab import drive
# drive.mount('/content/gdrive')

import pandas as pd, re
import numpy as np
import keras
from keras.models import Sequential
from keras.layers import Dense, Dropout
from keras.optimizers import RMSprop
from keras.preprocessing import image
from keras.applications.imagenet_utils import decode_predictions
from keras.applications.vgg16 import VGG16
from keras.applications.resnet50 import ResNet50
from keras.applications.resnet50 import preprocess_input
from keras.layers import Flatten, Dense, Dropout
from keras.layers.normalization import BatchNormalization
from keras.models import Model
from keras import optimizers
import random
import zipfile

import tensorflow as tf
from keras.backend.tensorflow_backend import set_session
config = tf.ConfigProto()
config.gpu_options.allow_growth = True  # dynamically grow the memory used on the GPU
sess = tf.Session(config=config)
set_session(sess)  # set this TensorFlow session as the default session for Keras


#model = VGG16(weights='imagenet')
model = ResNet50(weights='imagenet')

model.summary()

for i in range(len(model.layers)): model.layers[i].trainable = False

num_classes = 62

predictions = Dense(num_classes, activation = 'relu')(model.layers[-2].output)

head_model = Model(input = model.input, output = predictions)
head_model.summary()

head_model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])

import pandas as pd
df = pd.read_csv('/content/gdrive/My Drive/Public/data/meta.csv',sep='\t',index_col=1)

with zipfile.ZipFile('/content/gdrive/My Drive/Public/data/datasets.zip', 'r') as z:
     im_files = list(z.namelist())
     im_files = [x for x in im_files if ".jpg" in x and "user_images" not in x]
     idxs = range(0, len(im_files))
     bs = 300
     def get_batch(batch_size = 30):
          X = np.zeros((batch_size,224,224,3))
          y = np.zeros((batch_size, 62))
          ridxs = random.sample(idxs, batch_size)
          for i in range(batch_size):
               f = im_files[ridxs[i]]
               regex = "dataset.*?/(.*?)/.*?\.jp"
               label_str = re.findall(regex, f)[0]
               label_idx = int(df.ix[label_str].Nr)-1
               im = image.load_img(z.open(f), target_size=(224, 224))
               im = image.img_to_array(im)
               x = preprocess_input(np.expand_dims(im.copy(), axis=0))
               X[i, :] = x.copy()
               y[i,label_idx] = 1.0
          return X, y
     
     for i in range(500):
          X, y = get_batch(batch_size=bs)
          head_model.fit(X, y, epochs=1, batch_size=bs, verbose=1, validation_data=(X_val,y_val))

          

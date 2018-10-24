# %load_ext autoreload
# %autoreload 2
from __future__ import print_function
import pandas as pd
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

print ('loading')
model = VGG16()
print ('done')

img = image.load_img('dog.jpg', target_size=(224, 224))
img = image.img_to_array(img)
x = preprocess_input(np.expand_dims(img.copy(), axis=0))
preds = model.predict(x)
print (decode_predictions(preds, top=5))






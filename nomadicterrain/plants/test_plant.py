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
from keras.models import Model
from keras import optimizers
import random
import zipfile

#model = VGG16(weights='imagenet')
model = ResNet50(weights='imagenet')

model.summary()

num_classes = 62

predictions = Dense(num_classes, activation = 'relu')(model.layers[-2].output)

head_model = Model(input = model.input, output = predictions)
head_model.summary()

head_model.load_weights("/home/burak/Downloads/plant_resnet.h5")

img = image.load_img('/home/burak/Downloads/elepears.jpg', target_size=(224, 224))
img = image.img_to_array(img)
x = preprocess_input(np.expand_dims(img.copy(), axis=0))
preds = head_model.predict(x)
print (preds)
print (np.argmax(preds))
print (np.argsort(preds))

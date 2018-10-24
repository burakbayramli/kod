from __future__ import print_function
import os; os.environ['KERAS_BACKEND'] = 'theano'
import pandas as pd, keras, numpy as np
from keras.datasets import mnist
from keras.models import Sequential
from keras.layers import Dense, Dropout
from keras.optimizers import RMSprop
from keras.applications.resnet50 import ResNet50
from keras.preprocessing import image
from keras.applications.imagenet_utils import decode_predictions
from keras.applications.resnet50 import preprocess_input

print ('loading')
resnet = ResNet50(weights='imagenet')
print ('done')

img = image.load_img('dog.jpg', target_size=(224, 224))
img = image.img_to_array(img)
x = preprocess_input(np.expand_dims(img.copy(), axis=0))
preds = resnet.predict(x)
print (decode_predictions(preds, top=5))






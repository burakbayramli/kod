# %load_ext autoreload
# %autoreload 2
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

#for e in range(epochs):
#    print('Epoch', e)
#    batches = 0
#    for x_batch, y_batch in datagen.flow(x_train, y_train, batch_size=32):
#        model.fit(x_batch, y_batch)

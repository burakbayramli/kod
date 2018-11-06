#%load_ext autoreload
#%autoreload 2
from keras.applications.vgg16 import VGG16, preprocess_input
from keras.applications.vgg19 import VGG19, preprocess_input
from keras.applications.xception import Xception, preprocess_input
from keras.applications.resnet50 import ResNet50, preprocess_input
from keras.applications.inception_resnet_v2 import InceptionResNetV2, preprocess_input
from keras.applications.mobilenet import MobileNet, preprocess_input
from keras.applications.inception_v3 import InceptionV3, preprocess_input
from keras.preprocessing import image
from keras.models import Model
from keras.models import model_from_json
from keras.layers import Input
import re, collections, zipfile
import pandas as pd
import numpy as np

base_model = VGG16(weights="imagenet")
model = Model(input=base_model.input, output=base_model.get_layer('fc1').output)
print (model.summary())

with zipfile.ZipFile('/content/gdrive/My Drive/Public/data/europe_plants_images.zip', 'r') as z:
     im_files_orig = list(z.namelist())
    
print (im_files_orig[:5])
im_files = [x for x in im_files_orig if (".jpg" in x.lower() or ".jpeg" in x.lower())  and "bing/" in x]
print (im_files[:5])
label_dict = {}
for i in range(len(im_files)):
    f = im_files[i]
    regex = "bing/(.*?)/.*?\.jp"
    res = re.findall(regex, f)
    if len(res) > 0:
       label_str = res[0]
       if label_str not in label_dict: label_dict[label_str] = len(label_dict)


features = []
labels   = []

with zipfile.ZipFile('/content/gdrive/My Drive/Public/data/europe_plants_images.zip', 'r') as z:
     for i in range(len(im_files)):         
         f = im_files[i]
         if i % 100 == 0: print (f)
         regex = "bing/(.*?)/.*?\.jp"
         res = re.findall(regex, f)
         if len(res) > 0:
            label_str = res[0]
            label = label_dict[label_str]
            x = image.load_img(z.open(f), target_size=(224, 224))
       	    x = image.img_to_array(x)
       	    x = preprocess_input(np.expand_dims(x.copy(), axis=0))
            feature = model.predict(x)
            flat = feature.flatten()
            features.append(flat)
            labels.append(label)
         if i==10: break

import pickle
pickle.dump(labels, open("/content/gdrive/My Drive/Public/data/labels.pkl","wb"))
pickle.dump(features, open("/content/gdrive/My Drive/Public/data/features.pkl","wb"))
pickle.dump(label_dict, open("/content/gdrive/My Drive/Public/data/label_dict.pkl","wb"))



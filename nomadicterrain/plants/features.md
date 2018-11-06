
```python
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
import zipfile

base_model = VGG16(weights="imagenet")
model = Model(input=base_model.input, output=base_model.get_layer('fc1').output)
print (model.summary())
```

```text
_________________________________________________________________
Layer (type)                 Output Shape              Param #   
=================================================================
input_1 (InputLayer)         (None, 224, 224, 3)       0         
_________________________________________________________________
block1_conv1 (Conv2D)        (None, 224, 224, 64)      1792      
_________________________________________________________________
block1_conv2 (Conv2D)        (None, 224, 224, 64)      36928     
_________________________________________________________________
block1_pool (MaxPooling2D)   (None, 112, 112, 64)      0         
_________________________________________________________________
block2_conv1 (Conv2D)        (None, 112, 112, 128)     73856     
_________________________________________________________________
block2_conv2 (Conv2D)        (None, 112, 112, 128)     147584    
_________________________________________________________________
block2_pool (MaxPooling2D)   (None, 56, 56, 128)       0         
_________________________________________________________________
block3_conv1 (Conv2D)        (None, 56, 56, 256)       295168    
_________________________________________________________________
block3_conv2 (Conv2D)        (None, 56, 56, 256)       590080    
_________________________________________________________________
block3_conv3 (Conv2D)        (None, 56, 56, 256)       590080    
_________________________________________________________________
block3_pool (MaxPooling2D)   (None, 28, 28, 256)       0         
_________________________________________________________________
block4_conv1 (Conv2D)        (None, 28, 28, 512)       1180160   
_________________________________________________________________
block4_conv2 (Conv2D)        (None, 28, 28, 512)       2359808   
_________________________________________________________________
block4_conv3 (Conv2D)        (None, 28, 28, 512)       2359808   
_________________________________________________________________
block4_pool (MaxPooling2D)   (None, 14, 14, 512)       0         
_________________________________________________________________
block5_conv1 (Conv2D)        (None, 14, 14, 512)       2359808   
_________________________________________________________________
block5_conv2 (Conv2D)        (None, 14, 14, 512)       2359808   
_________________________________________________________________
block5_conv3 (Conv2D)        (None, 14, 14, 512)       2359808   
_________________________________________________________________
block5_pool (MaxPooling2D)   (None, 7, 7, 512)         0         
_________________________________________________________________
flatten (Flatten)            (None, 25088)             0         
_________________________________________________________________
fc1 (Dense)                  (None, 4096)              102764544 
=================================================================
Total params: 117,479,232
Trainable params: 117,479,232
Non-trainable params: 0
_________________________________________________________________
None
```

```python
image_size = (224, 224)

features = []
labels   = []

print ('images')
with zipfile.ZipFile('/home/burak/Downloads/campdata/europe_plants_images.zip', 'r') as z:
     im_files_orig = list(z.namelist())
    
```


```python
import re

print (im_files_orig[:5])
im_files = [x for x in im_files_orig if (".jpg" in x.lower() or ".jpeg" in x.lower())  and "bing/" in x]
print (im_files[:5])
for i in range(len(im_files)):
    f = im_files[i]
    regex = "bing/(.*?)/.*?\.jp"
    res = re.findall(regex, f)
    if len(res) > 0:
       label_str = res[0]
       #print (label_str)
print ('')
```

```text
['colors.png', 'dataset/', 'dataset/bing/', 'dataset/bing/Achillea millefolium/', 'dataset/bing/Achillea millefolium/Achillea millefolium.done']
['dataset/bing/Achillea millefolium/Scrapper_1.jpg', 'dataset/bing/Achillea millefolium/Scrapper_10.jpg', 'dataset/bing/Achillea millefolium/Scrapper_11.jpg', 'dataset/bing/Achillea millefolium/Scrapper_12.jpg', 'dataset/bing/Achillea millefolium/Scrapper_13.jpg']

```









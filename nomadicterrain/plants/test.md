

```python
import zipfile
with zipfile.ZipFile('/home/burak/Downloads/edible-wild-plants/datasets.zip', 'r') as z:
     for f in z.namelist():
     	 print f
```

```text
dataset/
dataset-test/
dataset-test/Alfalfa/
dataset-test/Alfalfa/Alfalfa_test1.jpeg
dataset-test/Alfalfa/Alfalfa_test2.jpg
dataset-test/Alfalfa/Alfalfa_test3.jpg
dataset-test/Alfalfa/Alfalfa_test4.jpg
dataset-test/Alfalfa/Alfalfa_test5.jpg
dataset-test/Asparagus/
dataset-test/Asparagus/asparagus-test1.jpg
dataset-test/Asparagus/asparagus-test2.jpg
dataset-test/Asparagus/asparagus-test3.jpg
dataset-test/Asparagus/asparagus-test4.jpg
dataset-test/Asparagus/asparagus-test5.jpg
dataset-test/Blue Vervain/
dataset-test/Blue Vervain/Blue_vervain_test1.jpg
```


```python
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
```


```python
import zipfile
with zipfile.ZipFile('/home/burak/Downloads/edible-wild-plants/datasets.zip', 'r') as z:
     im_files = list(z.namelist())
     print (im_files)
```

```python
import zipfile
with zipfile.ZipFile('/home/burak/Downloads/edible-wild-plants/datasets.zip', 'r') as z:
     f = 'dataset-test/Alfalfa/Alfalfa_test1.jpeg'
     im = image.load_img(z.open(f), target_size=(224, 224))
     im = image.img_to_array(im)
     print (im.shape)
```

```text
(224, 224, 3)
```


```python
import zipfile
with zipfile.ZipFile('/home/burak/Downloads/edible-wild-plants/datasets.zip', 'r') as z:
     im_files = list(z.namelist())
     im_files = np.array([x for x in im_files if ".jpg" in x])
     print (im_files)
     np.random.shuffle(im_files)

print (im_files)
```

```text
['dataset-test/Alfalfa/Alfalfa_test2.jpg'
 'dataset-test/Alfalfa/Alfalfa_test3.jpg'
 'dataset-test/Alfalfa/Alfalfa_test4.jpg' ...
 'dataset/Wood Sorrel/wood-sorrel7.jpg'
 'dataset/Wood Sorrel/wood-sorrel8.jpg'
 'dataset/Wood Sorrel/wood-sorrel9.jpg']
['dataset/Sunflower/5018120483_cc0421b176_m.jpg'
 'dataset/Sunflower/23247483352_0defc7a6dc_n.jpg'
 'dataset/Coneflower/Cone-Flower9.jpg' ...
 'dataset/Dandellion/4258272073_f616d1e575_m.jpg'
 'dataset/Downy Yellow Violet/Downy-Yellow-Violet6.jpg'
 'dataset/Daisy Fleabane/14372713423_61e2daae88.jpg']
```


```python
def get_batch(batch_size = 30):
    
```







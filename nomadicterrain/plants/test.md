
```python
import pandas as pd
df = pd.read_csv('meta.csv',sep='\t',index_col=1)
print (len(df.index))
print (df)
```

```text
62
                     Nr
Name                   
Alfalfa               9
Asparagus             5
Blue Vervain         46
Broadleaf Plantain   10
Bull Thistle          8
Cattail              45
Chickweed             3
Chicory               6
Cleavers             44
Coltsfoot            49
Common Sow Thistle   48
Common Yarrow        47
Coneflower           18
Creeping Charlie     11
Crimson Clover       52
Curly Dock            4
Daisy Fleabane       55
Dandellion            2
Downy Yellow Violet  54
Elderberry           16
Evening Primrose     53
Fern Leaf Yarrow     50
Field Pennycress     17
Fireweed              1
Forget Me Not        12
Garlic Mustard       13
Harebell             15
Henbit               51
Herb Robert          39
Japanese Knotweed    56
...                  ..
Kudzu                19
Lambs Quarters       58
Mallow               21
Mayapple             40
Meadowsweet          20
Milk Thistle         57
Mullein              25
New England Aster    61
Partridgeberry       27
Peppergrass          22
Pickerelweed         24
Pineapple Weed       23
Prickly Pear Cactus  38
Purple Deadnettle    60
Queen Annes Lace     59
Red Clover           26
Sheep Sorrel         28
Shepherds Purse      29
Spring Beauty        31
Sunflower            30
Supplejack Vine      62
Tea Plant            32
Teasel               34
Toothwort            33
Vervain Mallow       37
Wild Bee Balm        36
Wild Black Cherry    14
Wild Grape Vine      35
Wild Leek            43
Wood Sorrel           7

[62 rows x 1 columns]
```


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
import random
with zipfile.ZipFile('/home/burak/Downloads/edible-wild-plants/datasets.zip', 'r') as z:
     f = 'dataset-test/Alfalfa/Alfalfa_test1.jpeg'
     im = image.load_img(z.open(f), target_size=(224, 224))
     im = image.img_to_array(im)
     print (im.shape)
idxs = range(0, len(im_files))
     
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

print (im_files)
```

```text
['dataset-test/Alfalfa/Alfalfa_test2.jpg'
 'dataset-test/Alfalfa/Alfalfa_test3.jpg'
 'dataset-test/Alfalfa/Alfalfa_test4.jpg' ...
 'dataset/Wood Sorrel/wood-sorrel7.jpg'
 'dataset/Wood Sorrel/wood-sorrel8.jpg'
 'dataset/Wood Sorrel/wood-sorrel9.jpg']
['dataset-test/Alfalfa/Alfalfa_test2.jpg'
 'dataset-test/Alfalfa/Alfalfa_test3.jpg'
 'dataset-test/Alfalfa/Alfalfa_test4.jpg' ...
 'dataset/Wood Sorrel/wood-sorrel7.jpg'
 'dataset/Wood Sorrel/wood-sorrel8.jpg'
 'dataset/Wood Sorrel/wood-sorrel9.jpg']
```


```python
z = zipfile.ZipFile('/home/burak/Downloads/edible-wild-plants/datasets.zip', 'r')
def get_batch(batch_size = 30):
    X = np.zeros((batch_size,224,224,3))
    y = np.zeros((batch_size, 62))
    ridxs = random.sample(idxs,batch_size)
    for i in range(batch_size):    	
    	f = im_files[i]
	im = image.load_img(z.open(f), target_size=(224, 224))
	im = image.img_to_array(im)
	X[i, :] = im
    return X, y

X, y = get_batch()
print (y.shape)
```

```text
  File "<ipython-input-1-1f23be20f181>", line 10
    im = image.load_img(z.open(f), target_size=(224, 224))
                                                          ^
TabError: inconsistent use of tabs and spaces in indentation

```

```python
print (random.sample(idxs,10))
```

```text
[6640, 5481, 2977, 4759, 4845, 2394, 3611, 3858, 4024, 3967]
```




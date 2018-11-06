
ML

from keras.preprocessing import image
im = image.load_img(z.open(f), target_size=(224, 224))
image.save_img("out.png", im)

image
https://www.tensorflow.org/hub/tutorials/image_retraining#training_on_flowers
https://gogul09.github.io/software/flower-recognition-deep-learning

ResNet50 
https://github.com/CryoliteZ/Plants-Identification
https://www.kaggle.com/gaborfodor/resnet50-example
https://github.com/keras-team/keras/issues/3465

Collab

https://colab.research.google.com/drive/1S_9McYC_Hp0MIo9kS8p-zBkLjgz0KsIY
https://colab.research.google.com/drive/1rss61yJvHLdh42uJSv76QoUspRAY4J7K
http://cs229.stanford.edu/proj2016/report/LiuHuang-PlantLeafRecognition-report.pdf

Goog Collab GD file accesss
https://stackoverflow.com/questions/46986398/import-data-into-google-colaboratory


Scrape

python -u console.py bing dog --limit 10 --json

Dand

python ../../dand/dand.py dand.conf

Common European tree names

http://forest.jrc.ec.europa.eu/european-atlas-of-forest-tree-species/atlas-data-and-metadata/

Common European plant names

https://www.first-nature.com/flowers/index.php

Plant edibility data came from a combination of sources. First did a dump on

https://plants.sc.egov.usda.gov/adv_search.html

By enabling as many as edibility parameters, including scientific name etc.

Then scraped "Food", "Cuisine", "Culinary" headings on Wikipedia, by
passing the scientific name.

Then scraped PFAF by using

https://pfaf.org/user/Plant.aspx?LatinName=__name__

for scientific name.

The combined results is below:

https://www.dropbox.com/s/9xk33ruzvpmq57f/edible_plants.csv?dl=1


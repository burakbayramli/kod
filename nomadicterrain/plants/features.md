

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

with zipfile.ZipFile('/home/burak/Downloads/campdata/europe_plants_images.zip', 'r') as z:
     im_files_orig = list(z.namelist())
    
```


```python
import re, collections

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
print (label_dict)
```

```text
['colors.png', 'dataset/', 'dataset/bing/', 'dataset/bing/Achillea millefolium/', 'dataset/bing/Achillea millefolium/Achillea millefolium.done']
['dataset/bing/Achillea millefolium/Scrapper_1.jpg', 'dataset/bing/Achillea millefolium/Scrapper_10.jpg', 'dataset/bing/Achillea millefolium/Scrapper_11.jpg', 'dataset/bing/Achillea millefolium/Scrapper_12.jpg', 'dataset/bing/Achillea millefolium/Scrapper_13.jpg']
{'Armeria pungens': 24, 'Iris foetidissima': 121, 'Lonicera periclymenum': 146, 'Centaurea cyanus': 46, 'Ononis pusilla': 175, 'Crepis rubra': 63, 'Brassica oleracea': 34, 'Frasera speciosa': 92, 'Arctium minus': 21, 'Heracleum sphondylium': 115, 'Gladiolus italicus': 109, 'Saxifraga stellaris': 221, 'Cerinthe major': 50, 'Gentiana verna': 103, 'Lupinus arboreus': 149, 'Anemone nemorosa': 13, 'Hyoscyamus niger': 117, 'Nerium oleander': 171, 'Linaria repens': 139, 'Helleborus viridis': 113, 'Cynoglossum creticum': 65, 'Ajuga reptans': 4, 'Phlomis tuberosa': 194, 'Lotus corniculatus': 147, 'Campanula rotundifolia': 43, 'Trifolium pratense': 251, 'Lamium album': 127, 'Valeriana officinalis': 256, 'Aquilegia vulgaris': 19, 'Hyacinthoides non-scripta': 116, 'Impatiens capensis': 119, 'Reseda luteola': 214, 'Lithophragma parviflorum': 143, 'Prunus dulcis': 207, 'Fritillaria meleagris': 93, 'Asclepias speciosa': 27, 'Primula veris': 205, 'Bellardia trixago': 31, 'Lemna minor': 132, 'Lathyrus latifolius': 130, 'Menyanthes trifoliata': 160, 'Centaurium erythraea': 48, 'Antirrhinum majus': 16, 'Comarum palustre': 56, 'Misopates orontium': 163, 'Convolvulus tricolor': 59, 'Viola odorata': 264, 'Chenopodium vulvaria': 52, 'Pseudofumaria lutea': 208, 'Limonium vulgare': 138, 'Minuartia obtusiloba': 162, 'Lathyrus japonicus': 129, 'Parnassia palustris': 186, 'Anagallis tenella': 10, 'Lamium purpureum': 128, 'Goodyera repens': 112, 'Sonchus asper': 236, 'Ononis repens': 176, 'Erica lusitanica': 84, 'Phlomis fruticosa': 193, 'Medicago marina': 157, 'Nymphaea alba': 173, 'Crambe maritima': 62, 'Epipactis atrorubens': 79, 'Senecio jacobaea': 228, 'Linaria vulgaris': 140, 'Lysimachia nummularia': 151, 'Euphrasia officinalis': 90, 'Pedicularis groenlandica': 187, 'Geranium sanguineum': 107, 'Galium verum': 101, 'Opuntia ficus-indica': 178, 'Iris pseudacorus': 123, 'Arbutus unedo': 20, 'Lythrum salicaria': 153, 'Convolvulus arvensis': 58, 'Lepidium heterophyllum': 133, 'Geranium lucidum': 105, 'Senecio aquaticus': 227, 'Euphorbia characias': 89, 'Corallorhiza mertensiana': 60, 'Ranunculus hederaceus': 211, 'Dactylorhiza viridis': 67, 'Mentha spicata': 159, 'Centaurea nigra': 47, 'Potentilla reptans': 203, 'Sonchus oleraceus': 237, 'Parentucellia viscosa': 185, 'Glaucium corniculatum': 110, 'Cardamine pratensis': 45, 'Knautia arvensis': 126, 'Silene latifolia': 232, 'Vicia orobus': 261, 'Vicia cracca': 259, 'Liparis loeselii': 142, 'Spiranthes romanzoffiana': 240, 'Lobelia dortmanna': 145, 'Galeopsis tetrahit': 97, 'Anchusa azurea': 11, 'Gentianopsis thermalis': 104, 'Platanthera huronensis': 197, 'Eupatorium cannabinum': 88, 'Malva moschata': 154, 'Pinguicula vulgaris': 195, 'Hypericum humifusum': 118, 'Vicia benghalensis': 258, 'Butomus umbellatus': 35, 'Orobanche minor': 181, 'Aquilegia flavescens': 18, 'Myriophyllum spicatum': 167, 'Inula helenium': 120, 'Orobanche ramosa': 182, 'Myosotis scorpioides': 166, 'Lilium martagon': 135, 'Anagallis monelli': 9, 'Fumaria capreolata': 94, 'Ecballium elaterium': 76, 'Tripodion tetraphyllum': 253, 'Armeria maritima': 23, 'Cardamine bulbifera': 44, 'Calluna vulgaris': 38, 'Scolymus hispanicus': 224, 'Platanthera obtusata': 199, 'Geum rivale': 108, 'Filipendula ulmaria': 91, 'Tanacetum parthenium': 246, 'Calystegia soldanella': 41, 'Lithospermum officinale': 144, 'Potamogeton natans': 201, 'Callitriche stagnalis': 37, 'Adoxa moschatellina': 1, 'Cakile maritima': 36, 'Taraxacum officinale': 248, 'Reseda lutea': 213, 'Tussilago farfara': 254, 'Odontites vernus': 174, 'Dodecatheon pulchellum': 73, 'Yucca glauca': 265, 'Gaillardia aristata': 95, 'Astrantia major': 29, 'Ruta chalepensis': 217, 'Camassia quamash': 42, 'Baldellia ranunculoides': 30, 'Leucanthemum vulgare': 134, 'Datura stramonium': 68, 'Epilobium palustre': 78, 'Digitalis purpurea': 71, 'Orobanche crenata': 180, 'Althaea officinalis': 7, 'Arum italicum': 25, 'Pedicularis palustris': 188, 'Sparganium erectum': 239, 'Ajuga pyramidalis': 3, 'Dianthus deltoides': 69, 'Cypripedium acaule': 66, 'Solanum dulcamara': 235, 'Borago officinalis': 32, 'Claytonia sibirica': 54, 'Polygala vulgaris': 200, 'Succisa pratensis': 244, 'Colchicum autumnale': 55, 'Calypso bulbosa': 39, 'Penstemon whippleanus': 190, 'Serratula tinctoria': 230, 'Dracunculus vulgaris': 74, 'Calystegia sepium': 40, 'Vicia melanops': 260, 'Pedicularis sylvatica': 189, 'Erysimum cheiri': 87, 'Lupinus luteus': 150, 'Stellaria graminea': 243, 'Ratibida columnifera': 212, 'Achillea millefolium': 0, 'Scrophularia nodosa': 225, 'Muscari comosum': 164, 'Anthriscus sylvestris': 15, 'Linum narbonense': 141, 'Anemone coronaria': 12, 'Narcissus bulbocodium': 168, 'Symphytum officinale': 245, 'Jasione montana': 125, 'Veronica beccabunga': 257, 'Primula elatior': 204, 'Allium ursinum': 6, 'Medicago lupulina': 156, 'Stachys arvensis': 241, 'Eriophorum vaginatum': 86, 'Rosa canina': 216, 'Lavandula stoechas': 131, 'Mimulus guttatus': 161, 'Alliaria petiolata': 5, 'Viola biflora': 263, 'Narcissus pseudonarcissus': 170, 'Echium italicum': 77, 'Vinca minor': 262, 'Conium maculatum': 57, 'Chelidonium majus': 51, 'Argemone polyanthemos': 22, 'Sparganium emersum': 238, 'Geranium robertianum': 106, 'Iris lutescens': 122, 'Phacelia sericea': 192, 'Tripleurospermum maritimum': 252, 'Sarcocornia perennis': 220, 'Epipactis helleborine': 80, 'Drosera rotundifolia': 75, 'Silene gallica': 231, 'Sanguisorba officinalis': 219, 'Malva sylvestris': 155, 'Ornithogalum umbellatum': 179, 'Corallorhiza trifida': 61, 'Teucrium scorodonia': 249, 'Cistus ladanifer': 53, 'Dipsacus fullonum': 72, 'Gentiana parryi': 102, 'Asphodelus fistulosus': 28, 'Rhodiola rosea': 215, 'Oxalis articulata': 183, 'Mentha aquatica': 158, 'Onopordum illyricum': 177, 'Muscari neglectum': 165, 'Prunella vulgaris': 206, 'Epipactis palustris': 81, 'Centranthus ruber': 49, 'Galium saxatile': 100, 'Stachys palustris': 242, 'Salvia verbenaca': 218, 'Iris xiphium': 124, 'Cymbalaria muralis': 64, 'Scandix pecten-veneris': 223, 'Galium aparine': 98, 'Galium mollugo': 99, 'Lilium philadelphicum': 136, 'Silene vulgaris': 233, 'Anagallis arvensis': 8, 'Nuphar lutea': 172, 'Erica tetralix': 85, 'Typha latifolia': 255, 'Eranthis hyemalis': 82, 'Pulicaria dysenterica': 209, 'Saxifraga tridactylites': 222, 'Anthemis tinctoria': 14, 'Senecio vulgaris': 229, 'Arum maculatum': 26, 'Lysimachia vulgaris': 152, 'Erica cinerea': 83, 'Smyrnium olusatrum': 234, 'Platanthera hyperborea': 198, 'Papaver rhoeas': 184, 'Digitalis lanata': 70, 'Lupinus angustifolius': 148, 'Glaucium flavum': 111, 'Petasites hybridus': 191, 'Tanacetum vulgare': 247, 'Platanthera dilatata': 196, 'Potentilla erecta': 202, 'Bougainvillea glabra': 33, 'Limonium sinuatum': 137, 'Aegopodium podagraria': 2, 'Aquilegia elegantula': 17, 'Thalictrum aquilegifolium': 250, 'Galanthus nivalis': 96, 'Ranunculus flammula': 210, 'Sedum acre': 226, 'Narcissus papyraceus': 169, 'Heracleum mantegazzianum': 114}
```

```python
from sklearn.preprocessing import LabelEncoder

features = []
labels   = []

with zipfile.ZipFile('/home/burak/Downloads/campdata/europe_plants_images.zip', 'r') as z:
     for i in range(len(im_files)):
         f = im_files[i]
         regex = "bing/(.*?)/.*?\.jp"
         res = re.findall(regex, f)
         if len(res) > 0:
            label_str = res[0]
            label = label_dict[label_str]
            x = image.load_img(z.open(f), target_size=(224, 224))
       	    x = image.img_to_array(x)
            x = np.expand_dims(x, axis=0)
       	    x = preprocess_input(np.expand_dims(im.copy(), axis=0))
            feature = model.predict(x)
            flat = feature.flatten()
            features.append(flat)
            labels.append(label)
         break

le = LabelEncoder()
le_labels = le.fit_transform(labels)
print (le_labels)
```

```text
[0]
```







# Installation

## Termux

To run on termux

pkg install espeak espeak-dev python python-dev libjpeg-turbo-dev libcrypt-dev ndk-sysroot clang

LDFLAGS="-L/system/lib/" CFLAGS="-I/data/data/com.termux/files/usr/include/" pip install Pillow

`mpv` is also needed.

## Configuration File

GPS coordinates are retrieved from gpslogger, install this app first
before using this application and have gpslogger log
its output to a known file. The name of that file goes
under `gps` label in the config below.

Sample .nomadicterrain config (the two numbers after the zip filename are SCALEX and SCALEY). 

```
{
  "gps": "/home/burak/Downloads/gpslogger.csv",
  "mapzip": {"normal": <one of the settings below, as the default startup value>
	     "istanbul": ["/dir/dir/istanbul.zip",[23000,-35000]],
	     "berlin": ["/dir/dir/berlin.zip",[23000,-35000]],
	     "world1": ["/dir/dir/world1.zip",[45,-50]],
	     "world2": ["/dir/dir/world2.zip",[10,-17]],
	     "europe2": ["/dir/dir/europe2.zip",[2900,-4600]],
	     "europe3": ["/dir/dir/europe3.zip",[1450,-2400]],
	     "turkey1": ["/dir/dir/turkey1.zip",[1450,-1850]],
	     "turkey3": ["/dir/dir/turkey3.zip",[2900,-3500]]	     
  },
  "edible_plants": "/dir/dir/edible_plants.csv",
  "trails": "/dir/dir/trails",
  "news_dir": "/dir/dir",
  "guide_detail_dir": "[see ../guide/doc/details]",
  "spiller_pdf": "[see /ui/static/spiller.json]",
  "api": "[GOOGLE API ANAHTARI]",
  "weatherapi": "[openweathermap key]",
  "btype": "[BLOOD TYPE DIET csv, see ../guide/data/food.dat]",
  "hay": "[IRIS HAY data, see ../guide/doc/hay.txt]"
  "coordidx": "[DIR]/gps_coord_sample.npy",
  "elevdb": "/dir/dir/file.db",
  "poi": "/dir/dir/poi.csv"
}
```

## Data Files

### Map files

world2

https://drive.google.com/open?id=1H3KK9MWXXtxcKPw5LY_hAJEC14bX94jJ

istanbul.zip

https://drive.google.com/open?id=16kgu2-QOWShgJZOBp-6mReanzV5E2QRv

europe2.zip

https://drive.google.com/open?id=1qyQJkMt7mNuotGvTVOWCm9SUirtLZQ8P

berlin.zip

https://drive.google.com/open?id=18CtN_FHBOs47zvM3Uvirhgf4vM-5oMZB

## Water

Lines (small, narrow water resources) - https://drive.google.com/file/d/1Xv3hwem4F5j5JXSTMaoNd3JCqpihpyuR/view?usp=drivesdk

Polygons (bigger ones, lakes) - https://drive.google.com/file/d/18CvBtmtmwvqsLs0sMbtQPsybWcjQ3eyT/view?usp=drivesdk

### Other

GeoLiteCity

https://drive.google.com/open?id=1lLzsLklIhlAUButeHhsHxMjy6sBsrLte

Edible plants

https://drive.google.com/open?id=1t9470crpqWCKUlXwmK2DB9mvifBjAdCM

Points / Regions of Interests

https://drive.google.com/file/d/1e8ClH8WDSZTUW_MF17hTh2oZmu8m7CKK/view?usp=drivesdk

## Trails

You can use shared trails from others. Such data is shared on
wikiloc.com, sign-up and download, and drop the gpx files under
`trails` directory (defined above) and simply visit
`/trail/<file.gpx>` which will plot the trail.

## POI / ROI

The ROI file above can be searched by keyword. Any matching point or
region will be listed, once clicked, is shown as either a list of
points or single point. I included some camping locations as points in
the file, also some region based national parks. But region the user
can put anything in this file, for example the Berlin Wall / dividing
line between East and West? Put them here! 

## Elevation

From `/lineelev`, you can get elevation data displayed starting from
your location along points on a line pointing to any bearing chosen by
the user. Data comes from Google Elevation API. Make sure EA is
enabled for the api key (project) you entered in the config file.

But it is expensive to get and store elevation data granular enough to
be useful, for an entire terrain on the phone that can show mountains
and contour lines. We need to create elevation / topographic models
from sampled elevation data taken from Google Elevation API. The
approach explained below uses Radial Basis Functions method to
interpolate elevation data for any point in a region modeled by RBF.

First create the main table

```
conn = sqlite3.connect(params['elevdb'])
c = conn.cursor()
c.execute('''CREATE TABLE ELEVATION (latint INT, lonint INT, lat REAL, lon REAL, elevation REAL); ''')
```

which will be created in the database (file) defined in parameter
`elevdb`. 

For the model,

```
conn = sqlite3.connect(params['elevdb'])
c = conn.cursor()
c.execute('''DROP TABLE RBF1; ''')
c.execute('''CREATE TABLE RBF1 (latint INT, lonint INT, latlow REAL, lathigh REAL, lonlow REAL, lonhigh REAL, gamma REAL, W BLOB); ''')
```

We will take and store 40k sample elevation data points per
degree block, e.g. lat/lon 31-32 and 40-41 would be one degree
block. 0.001 degrees correspond to 100 meters.

Now for any lat/lon integer pair, you run `get_elev_data`, which
under-the-hood runs

* `insert_gps_int_rows` to insert 40k sample coordinates (they are the
same for every block) per block, with empty elevation values.  Sample
coordinates themselves (random numbers to be appended after decimal
points) can be created with `gen_gps_sample_coords`. These coordinates
are locations to be sampled within each block.

* `get_elev_goog` per block, to get its missing elevation data. This
call is restartable, will always work on missing data, so if it
crashes you can restart, it will continue from where it left off.

* `insert_rbf1_recs` for any block, takes raw data inserted earlier,
calculates and inserts RBF model parameters for block in `RBF1` table.

To use, now for any coordinate for blocks we have a model for, we can
run `/gotopo/lat;lon`.

Flattest Path

To get flattest path to a destination, visit `/flattestroute/lat;lon`. 

## Food

Plant edibility data came from a combination of sources. First did a
dump on

https://plants.sc.egov.usda.gov/adv_search.html

By enabling as many as edibility parameters, including scientific name etc.

Then scraped "Food", "Cuisine", "Culinary" headings on Wikipedia, by
passing the scientific name.

Then scraped PFAF by using

https://pfaf.org/user/Plant.aspx?LatinName=__name__

for scientific name.

Common European tree names

http://forest.jrc.ec.europa.eu/european-atlas-of-forest-tree-species/atlas-data-and-metadata/

Common European plant names

https://www.first-nature.com/flowers/index.php



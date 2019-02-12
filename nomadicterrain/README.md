
## Termux

To run on termux

pkg install python python-dev libjpeg-turbo-dev libcrypt-dev ndk-sysroot clang

LDFLAGS="-L/system/lib/" CFLAGS="-I/data/data/com.termux/files/usr/include/" pip install Pillow

## General

Some tools that might come in handy to run on smartphone (Android)
through Termux. 

GPS coordinates are retrieved from gpslogger, install this app first
before using this application and have gpslogger log
its output to a known file. The name of that file goes
under `gps` label in the config below.

Sample .nomadicterrain config (the two numbers after the zip filename are SCALEX and SCALEY). 

```
{
  "gps": "/home/burak/Downloads/gpslogger.csv",
  "nationalpark": "/home/burak/Downloads/campdata/national_parks.csv",
  "campsites": "/home/burak/Downloads/campdata/camping_locations.csv",
  "natpark_mindistance": 100.0,
  "mapzip": {"normal": <one of the settings below, as the default startup value>
	     "istanbul": ["/home/burak/Downloads/campdata/istanbul.zip",[23000,-35000]],
	     "berlin": ["/home/burak/Downloads/campdata/berlin.zip",[23000,-35000]],
	     "world1": ["/home/burak/Downloads/campdata/world1.zip",[45,-70]],
	     "world2": ["/home/burak/Downloads/campdata/world2.zip",[10,-17]],
	     "europe2": ["/home/burak/Downloads/campdata/europe2.zip",[2900,-4600]],
	     "europe3": ["/home/burak/Downloads/campdata/europe3.zip",[1450,-2400]],
	     "turkey1": ["/home/burak/Downloads/campdata/turkey1.zip",[1450,-1850]],
	     "turkey3": ["/home/burak/Downloads/campdata/turkey3.zip",[2900,-3500]]	     
  },
  "edible_plants": "/home/burak/Downloads/campdata/edible_plants.csv",
  "trails": "/home/burak/Downloads/campdata/trails",
  "guide_detail_dir": "/home/burak/Documents/kod/guide/doc/details",
  "spiller_pdf": "/home/burak/Documents/kod/nomadicterrain/ui/static/spiller.json",
  "api": "[GOOGLE API ANAHTARI]",
  "weatherapi": "[openweathermap anahtari]",
  "btype": "[BLOOD TYPE DIET csv, see ../guide/data/food.dat]",
  "hay": "[IRIS HAY data, see ../guide/doc/hay.txt]"
  "coordidx": "[DIR]/gps_coord_sample.npy",
  "elevdb": "/dir/dir/file.db",
}
```

## Files

### Map files

world2

https://drive.google.com/open?id=1H3KK9MWXXtxcKPw5LY_hAJEC14bX94jJ

istanbul.zip

https://drive.google.com/open?id=16kgu2-QOWShgJZOBp-6mReanzV5E2QRv

europe2.zip

https://drive.google.com/open?id=1qyQJkMt7mNuotGvTVOWCm9SUirtLZQ8P

berlin.zip

https://drive.google.com/open?id=18CtN_FHBOs47zvM3Uvirhgf4vM-5oMZB

### Other

GeoLiteCity

https://drive.google.com/open?id=1lLzsLklIhlAUButeHhsHxMjy6sBsrLte

Edible plants

https://drive.google.com/open?id=1t9470crpqWCKUlXwmK2DB9mvifBjAdCM

Camping locations

https://drive.google.com/open?id=12IouBuu18k1SYmxsseBluhQAHVFrtHsP

## Trails

You can use shared trails from others. Such data is shared on
wikiloc.com, sign-up and download, and drop the gpx files under
`trails` directory (defined above) and simply visit
`/trail/<file.gpx>` which will plot the trail.

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

First create the main table `create_elev_table` which will be created
in the database (file) defined in parameter `elevdb`. We will take and
store 40k sample elevation data points per degree block, e.g. lat/lon
31-32 and 40-41 would be one degree block. 0.001 degrees correspond to
100 meters.

Once table is created, run `insert_gps_int_rows` to insert 40k sample
*coordinates* (they are the same for every block) per block, with
empty elevation values.  Sample coordinates themselves (random numbers
to be appended after decimal points) can be created with
`gen_gps_sample_coords`. These coordinates are locations to be sampled
within each block.

Then run `get_elev_goog` per block, to get its missing elevation
data. This call is restartable, will always work on missing data, so
if it crashes you can restart, it will continue from where it left
off.

Now we are ready to create model. Run `create_rbf1_table` (one time).
Then, run `insert_rbf1_recs` for any block. This calculates and
inserts RBF model parameters for block in `RBF1` table.

To use, now for any coordinate for blocks we have a model for, we can
run `/gotopo/lat;lon`.

Flattest Path

To get flattest path to a destination, visit `/flattestroute/lat;lon`. 

## Food

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



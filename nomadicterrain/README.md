
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

Trails

You can use shared trails from others. Such data is shared on
wikiloc.com, sign-up and download, and drop the gpx files under
`trails` directory (defined above) and simply visit
`/trail/<file.gpx>` which will plot the trail.

Food

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



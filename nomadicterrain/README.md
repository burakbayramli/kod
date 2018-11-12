
Some tools that might come in handy to run on smartphone (Android)
through Termux. Every specific function will be in their respective
subdirectories which an outside process can communicate through its
in/ and out/ folders.

An Android UI can communicate with the programs as such: a central
dispatcher is brought up whcih constantly polls the in directories to
see if there are any inputs. If found, the inputs will be processed by
the program in the subdir. 

* map - given list of gps coordinates, it plots them on a map, outputs
  single image file.
* given a book file and from percentage to percentage parameters, it will extract
  the text from the file, and write it as text in out directory.

Sample .nomadicterrain config (the two numbers after the zip filename are SCALEX and SCALEY). 

```
{
  "gps": "/home/burak/Downloads/gpslogger.csv",
  "nationalpark": "/home/burak/Downloads/campdata/national_parks.csv",
  "campsites": "/home/burak/Downloads/campdata/camping_locations.csv",
  "natpark_mindistance": 100.0,
  "mapzip": {"normal": ["/home/burak/Downloads/campdata/europe2.zip",[2900,-4600]],
	     "terrain": ["/home/burak/Downloads/campdata/europe3.zip",[1450,-2400]],
	     "istanbul": ["/home/burak/Downloads/campdata/istanbul.zip",[23000,-35000]],
	     "berlin": ["/home/burak/Downloads/campdata/berlin.zip",[23000,-35000]],
	     "world1": ["/home/burak/Downloads/campdata/world1.zip",[45,-70]],
	     "world2": ["/home/burak/Downloads/campdata/world2.zip",[10,-17]]
  },
  "edible_plants": "/home/burak/Downloads/campdata/edible_plants.csv",
  "audio_output_folder": "/home/burak/Downloads",
  "guide_detail_dir": "/home/burak/Documents/kod/guide/doc/details"
}
```


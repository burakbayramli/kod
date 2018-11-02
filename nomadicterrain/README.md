
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

Sample .nomadicterrain config

{
  "gps": "/home/burak/Downloads/gpslogger.csv",
  "nationalpark": "/home/burak/Downloads/campdata/national_parks.csv",
  "campsites": "/home/burak/Downloads/campdata/camping_locations.csv",
  "natpark_mindistance": 100.0,
  "mapzip": "/home/burak/Downloads/campdata/europe2.zip",
  "edible_plants": "/home/burak/Downloads/campdata/edible_plants.csv",
  "audio_output_folder": "/home/burak/Downloads"
}
 


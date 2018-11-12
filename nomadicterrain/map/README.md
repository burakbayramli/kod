
python ../../dand/dand.py dand.conf 

To run on termux

pkg install python python-dev libjpeg-turbo-dev libcrypt-dev ndk-sysroot clang

LDFLAGS="-L/system/lib/" CFLAGS="-I/data/data/com.termux/files/usr/include/" pip install Pillow

An example config file

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
	     "world2": ["/home/burak/Downloads/campdata/world2.zip",[100,-100]]
  },
  "edible_plants": "/home/burak/Downloads/campdata/edible_plants.csv",
  "audio_output_folder": "/home/burak/Downloads",
  "guide_detail_dir": "/home/burak/Documents/kod/guide/doc/details"
}

Map files

https://www.dropbox.com/s/uo1u5fps9u4ki36/istanbul.zip?dl=1

https://www.dropbox.com/s/exu9dqhz5kquloo/berlin.zip?dl=1

https://www.dropbox.com/s/tmgdjekhfiftve6/world1.zip?dl=1

https://www.dropbox.com/s/xnm2kc77xb6aq0d/world2.zip?dl=1





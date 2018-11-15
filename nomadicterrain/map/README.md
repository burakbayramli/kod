
python ../../dand/dand.py dand.conf 

To run on termux

pkg install python python-dev libjpeg-turbo-dev libcrypt-dev ndk-sysroot clang

LDFLAGS="-L/system/lib/" CFLAGS="-I/data/data/com.termux/files/usr/include/" pip install Pillow

See ../README for an example config file

Map files are shared in the ../README.md




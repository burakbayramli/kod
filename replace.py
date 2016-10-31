# replace.py - search and replace through python, run it like this
# sh -c 'find ./test -type f -name "*" -exec python replace.py {} \;'
# the code to change stuff goes below
import os, re, sys
filename = sys.argv[1]
content = open(filename).read()
fout = open(filename,"w")

# you can insert stuff
# fout.write("import bla\n")

# or replace stuff through regex
# content = content.replace("burak","bayramli")

fout.write(content)
fout.close()

# coding=utf-8
import urllib
import re
import os

infile = open("/tmp/hatdetaydurakutf")
outfile = open("/tmp/hatdetayduraknospaceutf", "w")
for line in infile.readlines():    
    if (line != "\n"):
        outfile.write(line)
        outfile.flush()
    
infile.close()
outfile.close()

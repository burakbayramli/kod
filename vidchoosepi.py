import glob, os, random, sys
import threading, numpy as np
import datetime, random
from rsync import ls
import select, rndplay

dir = "/media/pi/Seagate Backup Plus Drive/shows"
dirs,list = ls(dir)
print ("Files", len(list))
idx = rndplay.my_random(len(list))
print ("show idx selected", idx, "song", list[idx][0])


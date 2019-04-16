import geopy.distance
import numpy as np
from urllib.request import urlopen
import os, json, re

def match(ms, s):
    return not re.search(s,ms,re.IGNORECASE) is None

print (match("hospital","hospital"))




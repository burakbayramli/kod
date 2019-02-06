import pandas as pd
import geopy.distance
import numpy as np
import matplotlib.pyplot as plt

df = pd.read_csv("/data/data/com.termux/files/home/Downloads/alanelev.csv")

lat1,lon1 = (36.549177, 31.981221)

d = df.apply(lambda x:geopy.distance.vincenty((x['lat'], x['lon']),(lat1,lon1)).km,axis=1)

df[d<10.0].to_csv("/data/data/com.termux/files/home/Downloads/alanelev2.csv",index=None)

#print (d.head(100))

# coding=utf-8
import loader
import pickle
import numpy as np
from create import *
import itertools

duraklar, next_stops, sr, routestops, k = loader.load()

print "durak", duraklar['L0139J'] 
print "durak", duraklar['Þ0005A']
print "durak", duraklar['Þ0015L'] # taksim

print "next stops", next_stops['L0168A']
print "next stops", next_stops['L0167B']
print "next stops", next_stops['L0166B']
print "next stops", next_stops['L0153D']

print "next stops", next_stops['A0617B']
print "next stops", next_stops['L0167B']

print "sr", sr['L0168A']
print "sr", sr['Þ0015L']
print "sr", sr['L0152A']
print "sr", sr['L0152B']

print 'routestops', routestops['59N']

print "K", K(k, '59N', 'L0168A')


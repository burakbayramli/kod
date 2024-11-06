import os, sys
sys.path.append("/home/burak/Documents/tw/en/mbl/2024")
import util

if sys.argv[1] == "538":
    util.kh_djt_538_polls()
if sys.argv[1] == "ukr":
    util.map_ukraine_suriyak()
if sys.argv[1] == "sudan":
    util.map_sahel_suriyak()
if sys.argv[1] == "lebanon":
    util.map_isr_suriyak()
    

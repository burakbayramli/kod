import os, sys
sys.path.append("/home/burak/Documents/tw/en/mbl/2025")
import util

if sys.argv[1] == "ukr":
    util.map_ukraine_suriyak()
    
if sys.argv[1] == "sudan":
    util.map_sahel_suriyak()
    
if sys.argv[1] == "isr":
    util.map_isr_suriyak()
    
if sys.argv[1] == "approv":
    util.trump_approval()
    

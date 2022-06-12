import numpy as np, os

full = open("/sys/class/power_supply/BAT0/charge_full").read()
full = float(full)
now = open("/sys/class/power_supply/BAT0/charge_now").read()
now = float(now)
b = now / full
print ((int(b * 100)), '%')
print
print (os.system("lsb_release -a"))

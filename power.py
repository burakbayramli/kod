import numpy as np
full = open("/sys/class/power_supply/BAT0/charge_full").read()
full = float(full)
now = open("/sys/class/power_supply/BAT0/charge_now").read()
now = float(now)
b = now / full
b = str(b)[2:4]
print (b, '%')

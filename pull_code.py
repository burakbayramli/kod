import os, sys, re

fout = open("pout1111.py","w")
fout.write("""
import numpy as np
import matplotlib.pyplot as plt
""")
fname = sys.argv[1]
content = open(fname).read()
res = re.findall("```python(.*?)```", content, re.DOTALL)
for x in res: fout.write(x)
fout.close()

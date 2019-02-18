import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

x = np.array([[1,1],[4,4],[2,2],[6,6]])

steps = np.linspace(x[:,0].min(), x[:,0].max(),100.0)

print (np.interp(steps, x[:,0], x[:,1]))


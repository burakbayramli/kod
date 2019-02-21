from scipy.interpolate import Rbf
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

A = np.random.rand(4, 50)
print (A.shape)

x, y, z, d = np.random.rand(4, 50)
print (z.shape)
print (d.shape)
rbfi = Rbf(x, y, z, d)  # radial basis function interpolator instance
xi = yi = zi = np.linspace(0, 1, 20)
di = rbfi(xi, yi, zi)   # interpolated values
di.shape


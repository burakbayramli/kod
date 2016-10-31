from distutils.core import setup, Extension
import os

setup(name='siftpy1',
      version='1.0',
      py_modules=['siftpy1'],
      ext_modules = [Extension('siftimp',
                               ['sift-driver.cpp','sift.cpp'],
                               include_dirs=['.']) ]
)

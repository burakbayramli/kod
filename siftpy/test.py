import pandas as pd
import siftpy1
import matplotlib.pylab as plt
from PIL import Image

df1 = siftpy1.sift("crans_1_small.pgm",threshold=10.0)
print len(df1)

im=Image.open("crans_2_small.jpg")
df1.plot(kind='scatter',x=0,y=1)
plt.hold(True)
plt.imshow(im)
plt.savefig('test_02.png')

df2 = siftpy1.sift("crans_2_small.pgm",threshold=10.0)

im=Image.open("crans_2_small.jpg")
df2.plot(kind='scatter',x=0,y=1)
plt.hold(True)
plt.imshow(im)
plt.savefig('test_02.png')



















































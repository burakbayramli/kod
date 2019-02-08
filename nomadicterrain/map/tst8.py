import numpy as np, gzip, binascii, codecs, base64
import pandas as pd
import pickle


a = np.array([[3,4,45]]).T
a = pd.DataFrame(a)
print (a)

e = pickle.dumps(a)
print (e)

df = pickle.loads(e)
print (df)

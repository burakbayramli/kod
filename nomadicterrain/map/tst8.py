import numpy as np, gzip, binascii, codecs, base64

a = b'asdjflakjdsflkjalskjflkaslkf'
a = base64.b32encode(a).encode('UTF-8')
print (a)
print (type(a))

a = np.array([[3,4,45]])
print (base64.b32encode(a))


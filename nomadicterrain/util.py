import numpy as np

"""
    p1 = np.array([[1, 2]])
    p2 = np.array([[4, 6], [2, 1], [7, 8]])
    my_cdist(p1,p2)
    print (cdist(p1,p2))
"""
def cdist(p1,p2):    
    distances = np.linalg.norm(p1 - p2, axis=1)
    return distances

import numpy as np
import matplotlib.pyplot as plt
import plot_map

#pts = np.array([[42.657889, 18.087956],[42.674881, 18.144730]])
#pts = np.array([[51.218343232,4.404985494]])
#pts = np.array([[51.213052000000005,4.44]])
#plot_map.plot(pts,'out.png')

park1 = [[51.198689, 4.386747],[51.192246, 4.428266],[51.221485, 4.452641],[51.235894, 4.422429]]
park2 = [[51.238689, 4.406747],[51.232246, 4.444266],[51.251485, 4.472641],[51.265894, 4.452429]]
parks = [park1, park2]
#parks = [park1]
pt = [51.213052000000005,4.44]

#plot_map.plot(park1,'out.png')

plot_map.plot_area(pt, parks, 'out.png')


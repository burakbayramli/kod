import os, sys

if sys.argv[1] == "sim":
    sys.path.append("/home/burak/Documents/classnotes/sk/2026/01")
    import simrecom
    simrecom.recommend()
    
if sys.argv[1] == "pmf":
    sys.path.append("/home/burak/Documents/classnotes/stat/stat_140_pmf/pmf")
    import recom
    recom.recommend()
    


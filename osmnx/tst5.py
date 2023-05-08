from diskdict import DiskDict

nodes = DiskDict('/tmp/nodes_dict')

#for k in nodes.keys():
#    print (k,nodes[k])
    
paths = DiskDict('/tmp/paths_dict')

for k in paths.keys():
    print (k,paths[k])

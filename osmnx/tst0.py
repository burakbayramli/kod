import osmnx as ox, networkx as nx

G = ox.graph_from_xml("/opt/Downloads/osm/seychelles-latest.osm.bz2")
nx.write_edgelist(G, "test.edgelist.gz",data=["oneway","length"])

print (len(G))

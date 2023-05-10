import itertools, datetime as dt, bz2, xml.sax
from pathlib import Path
from xml.etree import ElementTree as etree
import networkx as nx
import numpy as np
import matplotlib.pyplot as plt
from diskdict import DiskDict
import warnings
from memory_profiler import profile
import gc

all_oneway = False
useful_tags_node = ["ref", "highway"]

useful_tags_way = [ "bridge", "tunnel", "oneway", "lanes", "ref",
                    "name", "highway", "maxspeed", "service", "access", "area",
                    "landuse", "width", "est_width", "junction", ]

def _is_path_reversed(path, reversed_values):
    if "oneway" in path and path["oneway"] in reversed_values:
        return True
    else:
        return False


def _is_path_one_way(path, bidirectional, oneway_values):
    if all_oneway:
        return True

    elif bidirectional:
        return False

    elif "oneway" in path and path["oneway"] in oneway_values:
        return True

    elif "junction" in path and path["junction"] == "roundabout":
        return True

    else:
        return False
            
def _convert_path(element,path_holder):

    path_holder["osmid"] = element["id"]

    path_holder["nodes"] = [group[0] for group in itertools.groupby(element["nodes"])]

    if "tags" in element:
        for useful_tag in useful_tags_way:
            if useful_tag in element["tags"]:
                path_holder[useful_tag] = element["tags"][useful_tag]

def _convert_node(element, node_holder):

    node_holder["y"] = element["lat"]
    node_holder["x"] = element["lon"]
    if "tags" in element:
        for useful_tag in useful_tags_node:
            if useful_tag in element["tags"]:
                node_holder[useful_tag] = element["tags"][useful_tag]

class _OSMContentHandler(xml.sax.handler.ContentHandler):

    def __init__(self,outputDir='/tmp'):
        self.object = DiskDict(outputDir + "/object_dict")
        self.object["elements"] = list()

    def startElement(self, name, attrs):
        if name == "osm":
            for k, v in attrs.items():
                if k in {"version", "generator"}: self.object[k] = v

        elif name in {"node", "way"}:
            self._element = dict(type=name, tags={}, nodes=[], **attrs)
            self._element.update({k: float(v) for k, v in attrs.items() if k in {"lat", "lon"}})
            self._element.update(
                {k: int(v) for k, v in attrs.items() if k in {"id", "uid", "version", "changeset"}}
            )

        elif name == "relation":
            self._element = dict(type=name, tags={}, members=[], **attrs)
            self._element.update(
                {k: int(v) for k, v in attrs.items() if k in {"id", "uid", "version", "changeset"}}
            )

        elif name == "tag":
            self._element["tags"].update({attrs["k"]: attrs["v"]})

        elif name == "nd":
            self._element["nodes"].append(int(attrs["ref"]))

        elif name == "member":
            self._element["members"].append(
                {k: (int(v) if k == "ref" else v) for k, v in attrs.items()}
            )
        

            
    def endElement(self, name):
        if name in {"node", "way", "relation"}:
            tmp = self.object["elements"]
            tmp.append(self._element)
            self.object["elements"] = tmp

def _overpass_json_from_file(filepath, outputDir):

    def _opener(filepath):
        if filepath.suffix == ".bz2":
            return bz2.BZ2File(filepath)
        else:
            return filepath.open(mode="rb")

    with _opener(Path(filepath)) as f:
        handler = _OSMContentHandler(outputDir)
        xml.sax.parse(f, handler)
        return handler.object

def _create_dict(response_json, retain_all=False, bidirectional=False, outputDir='/tmp'):

    if not any(response_json["elements"]):  # pragma: no cover
        raise EmptyOverpassResponse("There are no data elements in the response JSON")

    nodes = DiskDict(outputDir + '/nodes_dict')
    paths = DiskDict(outputDir + '/paths_dict')
    node_holder = {}
    path_holder = {}
    for element in response_json["elements"]:
        node_holder.clear()
        path_holder.clear()
        if element["type"] == "node":
            _convert_node(element,node_holder)
            nodes[element["id"]] = node_holder
        elif element["type"] == "way":
            _convert_path(element,path_holder)
            paths[element["id"]] = path_holder
    
#filepath = '/mnt/3d1ece2f-6539-411b-bac2-589d57201626/home/burak/Downloads/osm/seychelles-latest.osm.bz2'
filepath = '/mnt/3d1ece2f-6539-411b-bac2-589d57201626/home/burak/Downloads/osm/luxembourg-latest.osm.bz2'
#filepath = '/home/burak/Documents/repos/osmnx/tests/input_data/West-Oakland.osm.bz2'
                
if __name__ == "__main__":

    out = '/tmp'
    warnings.filterwarnings('ignore') 
    j = _overpass_json_from_file(filepath,outputDir=out)
    _create_dict(j,outputDir=out)

    

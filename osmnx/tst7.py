import itertools, datetime as dt, bz2, xml.sax
from pathlib import Path
from xml.etree import ElementTree as etree
import networkx as nx
import numpy as np
import matplotlib.pyplot as plt
from diskdict import DiskDict
import warnings

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

def _add_paths(G, paths, bidirectional=False):

    oneway_values = {"yes", "true", "1", "-1", "reverse", "T", "F"}
    reversed_values = {"-1", "reverse", "T"}

    for path in paths:
        nodes = path.pop("nodes")

        is_one_way = _is_path_one_way(path, bidirectional, oneway_values)
        if is_one_way and _is_path_reversed(path, reversed_values):
            nodes.reverse()

        if not all_oneway:
            path["oneway"] = is_one_way

        edges = list(zip(nodes[:-1], nodes[1:]))

        path["reversed"] = False
        G.add_edges_from(edges, **path)

        if not is_one_way:
            path["reversed"] = True
            G.add_edges_from([(v, u) for u, v in edges], **path)
            
def _convert_path(element):

    path = {"osmid": element["id"]}

    path["nodes"] = [group[0] for group in itertools.groupby(element["nodes"])]

    if "tags" in element:
        for useful_tag in useful_tags_way:
            if useful_tag in element["tags"]:
                path[useful_tag] = element["tags"][useful_tag]
    return path

def _convert_node(element):

    node = {"y": element["lat"], "x": element["lon"]}
    if "tags" in element:
        for useful_tag in useful_tags_node:
            if useful_tag in element["tags"]:
                node[useful_tag] = element["tags"][useful_tag]
    return node


class _OSMContentHandler(xml.sax.handler.ContentHandler):

    def __init__(self):
        self._element = None
        self.object = {"elements": []}

    def startElement(self, name, attrs):
        if name == "osm":
            self.object.update({k: v for k, v in attrs.items() if k in {"version", "generator"}})

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
            self.object["elements"].append(self._element)


def _overpass_json_from_file(filepath):

    def _opener(filepath):
        if filepath.suffix == ".bz2":
            return bz2.BZ2File(filepath)
        else:
            return filepath.open(mode="rb")

    with _opener(Path(filepath)) as f:
        handler = _OSMContentHandler()
        xml.sax.parse(f, handler)
        return handler.object

def _create_dict(response_json, retain_all=False, bidirectional=False, outputDir='/tmp'):

    if not any(response_json["elements"]):  # pragma: no cover
        raise EmptyOverpassResponse("There are no data elements in the response JSON")

    nodes = DiskDict(outputDir + '/nodes_dict')
    paths = DiskDict(outputDir + '/paths_dict')
    for element in response_json["elements"]:
        if element["type"] == "node":
            nodes[element["id"]] = _convert_node(element)
        elif element["type"] == "way":
            paths[element["id"]] = _convert_path(element)
    
#filepath = '/mnt/3d1ece2f-6539-411b-bac2-589d57201626/home/burak/Downloads/osm/seychelles-latest.osm.bz2'
filepath = '/home/burak/Documents/repos/osmnx/tests/input_data/West-Oakland.osm.bz2'
                
if __name__ == "__main__":

    warnings.filterwarnings('ignore') 
    j = _overpass_json_from_file(filepath)
    _create_dict(j)

    

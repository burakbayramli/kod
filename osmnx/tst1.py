import itertools
import datetime as dt
import bz2
import xml.sax
from pathlib import Path
from xml.etree import ElementTree as etree
import networkx as nx
import numpy as np
import pandas as pd

EARTH_RADIUS_M = 6_371_009
all_oneway = False
useful_tags_node = ["ref", "highway"]
useful_tags_way = [
    "bridge",
    "tunnel",
    "oneway",
    "lanes",
    "ref",
    "name",
    "highway",
    "maxspeed",
    "service",
    "access",
    "area",
    "landuse",
    "width",
    "est_width",
    "junction",
]

def great_circle_vec(lat1, lng1, lat2, lng2, earth_radius=EARTH_RADIUS_M):
    """
    Calculate great-circle distances between pairs of points.

    Vectorized function to calculate the great-circle distance between two
    points' coordinates or between arrays of points' coordinates using the
    haversine formula. Expects coordinates in decimal degrees.

    Parameters
    ----------
    lat1 : float or numpy.array of float
        first point's latitude coordinate
    lng1 : float or numpy.array of float
        first point's longitude coordinate
    lat2 : float or numpy.array of float
        second point's latitude coordinate
    lng2 : float or numpy.array of float
        second point's longitude coordinate
    earth_radius : float
        earth's radius in units in which distance will be returned (default is
        meters)

    Returns
    -------
    dist : float or numpy.array of float
        distance from each (lat1, lng1) to each (lat2, lng2) in units of
        earth_radius
    """
    y1 = np.deg2rad(lat1)
    y2 = np.deg2rad(lat2)
    dy = y2 - y1

    x1 = np.deg2rad(lng1)
    x2 = np.deg2rad(lng2)
    dx = x2 - x1

    h = np.sin(dy / 2) ** 2 + np.cos(y1) * np.cos(y2) * np.sin(dx / 2) ** 2
    h = np.minimum(1, h)  # protect against floating point errors
    arc = 2 * np.arcsin(np.sqrt(h))

    # return distance in units of earth_radius
    return arc * earth_radius

def add_edge_lengths(G, precision=3, edges=None):
    """
    Add `length` attribute (in meters) to each edge.

    Vectorized function to calculate great-circle distance between each edge's
    incident nodes. Ensure graph is in unprojected coordinates, and
    unsimplified to get accurate distances.

    Note: this function is run by all the `graph.graph_from_x` functions
    automatically to add `length` attributes to all edges. It calculates edge
    lengths as the great-circle distance from node `u` to node `v`. When
    OSMnx automatically runs this function upon graph creation, it does it
    before simplifying the graph: thus it calculates the straight-line lengths
    of edge segments that are themselves all straight. Only after
    simplification do edges take on a (potentially) curvilinear geometry. If
    you wish to calculate edge lengths later, you are calculating
    straight-line distances which necessarily ignore the curvilinear geometry.
    You only want to run this function on a graph with all straight edges
    (such as is the case with an unsimplified graph).

    Parameters
    ----------
    G : networkx.MultiDiGraph
        unprojected, unsimplified input graph
    precision : int
        decimal precision to round lengths
    edges : tuple
        tuple of (u, v, k) tuples representing subset of edges to add length
        attributes to. if None, add lengths to all edges.

    Returns
    -------
    G : networkx.MultiDiGraph
        graph with edge length attributes
    """
    if edges is None:
        uvk = tuple(G.edges)
    else:
        uvk = edges

    # extract edge IDs and corresponding coordinates from their nodes
    x = G.nodes(data="x")
    y = G.nodes(data="y")
    try:
        # two-dimensional array of coordinates: y0, x0, y1, x1
        c = np.array([(y[u], x[u], y[v], x[v]) for u, v, k in uvk])
    except KeyError:  # pragma: no cover
        raise KeyError("some edges missing nodes, possibly due to input data clipping issue")

    # calculate great circle distances, round, and fill nulls with zeros
    dists = great_circle_vec(c[:, 0], c[:, 1], c[:, 2], c[:, 3]).round(precision)
    dists[np.isnan(dists)] = 0
    nx.set_edge_attributes(G, values=dict(zip(uvk, dists)), name="length")

    print("Added length attributes to graph edges")
    return G

def _is_path_reversed(path, reversed_values):
    """
    Determine if the order of nodes in a path should be reversed.

    Parameters
    ----------
    path : dict
        a path's tag:value attribute data
    reversed_values : set
        the values OSM uses in its 'oneway' tag to denote travel can only
        occur in the opposite direction of the node order

    Returns
    -------
    bool
    """
    if "oneway" in path and path["oneway"] in reversed_values:
        return True
    else:
        return False


def _is_path_one_way(path, bidirectional, oneway_values):
    """
    Determine if a path of nodes allows travel in only one direction.

    Parameters
    ----------
    path : dict
        a path's tag:value attribute data
    bidirectional : bool
        whether this is a bi-directional network type
    oneway_values : set
        the values OSM uses in its 'oneway' tag to denote True

    Returns
    -------
    bool
    """
    # rule 1
    if all_oneway:
        # if globally configured to set every edge one-way, then it's one-way
        return True

    # rule 2
    elif bidirectional:
        # if this is a bi-directional network type, then nothing in it is
        # considered one-way. eg, if this is a walking network, this may very
        # well be a one-way street (as cars/bikes go), but in a walking-only
        # network it is a bi-directional edge (you can walk both directions on
        # a one-way street). so we will add this path (in both directions) to
        # the graph and set its oneway attribute to False.
        return False

    # rule 3
    elif "oneway" in path and path["oneway"] in oneway_values:
        # if this path is tagged as one-way and if it is not a bi-directional
        # network type then we'll add the path in one direction only
        return True

    # rule 4
    elif "junction" in path and path["junction"] == "roundabout":
        # roundabouts are also one-way but are not explicitly tagged as such
        return True

    else:
        # otherwise this path is not tagged as a one-way
        return False

def _add_paths(G, paths, bidirectional=False):
    """
    Add a list of paths to the graph as edges.

    Parameters
    ----------
    G : networkx.MultiDiGraph
        graph to add paths to
    paths : list
        list of paths' tag:value attribute data dicts
    bidirectional : bool
        if True, create bi-directional edges for one-way streets

    Returns
    -------
    None
    """
    # the values OSM uses in its 'oneway' tag to denote True, and to denote
    # travel can only occur in the opposite direction of the node order. see:
    # https://wiki.openstreetmap.org/wiki/Key:oneway
    # https://www.geofabrik.de/de/data/geofabrik-osm-gis-standard-0.7.pdf
    oneway_values = {"yes", "true", "1", "-1", "reverse", "T", "F"}
    reversed_values = {"-1", "reverse", "T"}

    for path in paths:
        # extract/remove the ordered list of nodes from this path element so
        # we don't add it as a superfluous attribute to the edge later
        nodes = path.pop("nodes")

        # reverse the order of nodes in the path if this path is both one-way
        # and only allows travel in the opposite direction of nodes' order
        is_one_way = _is_path_one_way(path, bidirectional, oneway_values)
        if is_one_way and _is_path_reversed(path, reversed_values):
            nodes.reverse()

        # set the oneway attribute, but only if when not forcing all edges to
        # oneway with the all_oneway setting. With the all_oneway setting, you
        # want to preserve the original OSM oneway attribute for later clarity
        if not all_oneway:
            path["oneway"] = is_one_way

        # zip path nodes to get (u, v) tuples like [(0,1), (1,2), (2,3)].
        edges = list(zip(nodes[:-1], nodes[1:]))

        # add all the edge tuples and give them the path's tag:value attrs
        path["reversed"] = False
        G.add_edges_from(edges, **path)

        # if the path is NOT one-way, reverse direction of each edge and add
        # this path going the opposite direction too
        if not is_one_way:
            path["reversed"] = True
            G.add_edges_from([(v, u) for u, v in edges], **path)
            
def _convert_path(element):
    """
    Convert an OSM way element into the format for a networkx path.

    Parameters
    ----------
    element : dict
        an OSM way element

    Returns
    -------
    path : dict
    """
    path = {"osmid": element["id"]}

    # remove any consecutive duplicate elements in the list of nodes
    path["nodes"] = [group[0] for group in itertools.groupby(element["nodes"])]

    if "tags" in element:
        for useful_tag in useful_tags_way:
            if useful_tag in element["tags"]:
                path[useful_tag] = element["tags"][useful_tag]
    return path

def _convert_node(element):
    """
    Convert an OSM node element into the format for a networkx node.

    Parameters
    ----------
    element : dict
        an OSM node element

    Returns
    -------
    node : dict
    """
    node = {"y": element["lat"], "x": element["lon"]}
    if "tags" in element:
        for useful_tag in useful_tags_node:
            if useful_tag in element["tags"]:
                node[useful_tag] = element["tags"][useful_tag]
    return node

def _parse_nodes_paths(response_json):
    """
    Construct dicts of nodes and paths from an Overpass response.

    Parameters
    ----------
    response_json : dict
        JSON response from the Overpass API

    Returns
    -------
    nodes, paths : tuple of dicts
        dicts' keys = osmid and values = dict of attributes
    """
    nodes = {}
    paths = {}
    for element in response_json["elements"]:
        if element["type"] == "node":
            nodes[element["id"]] = _convert_node(element)
        elif element["type"] == "way":
            paths[element["id"]] = _convert_path(element)

    return nodes, paths

class _OSMContentHandler(xml.sax.handler.ContentHandler):
    """
    SAX content handler for OSM XML.

    Used to build an Overpass-like response JSON object in self.object. For
    format notes, see
    http://wiki.openstreetmap.org/wiki/OSM_XML#OSM_XML_file_format_notes and
    http://overpass-api.de/output_formats.html#json
    """

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
    """
    Read OSM XML from file and return Overpass-like JSON.

    Parameters
    ----------
    filepath : string or pathlib.Path
        path to file containing OSM XML data

    Returns
    -------
    OSMContentHandler object
    """

    def _opener(filepath):
        if filepath.suffix == ".bz2":
            return bz2.BZ2File(filepath)
        else:
            # assume an unrecognized file extension is just XML
            return filepath.open(mode="rb")

    with _opener(Path(filepath)) as f:
        handler = _OSMContentHandler()
        xml.sax.parse(f, handler)
        return handler.object

def get_largest_component(G, strongly=False):
    """
    Get subgraph of G's largest weakly/strongly connected component.

    Parameters
    ----------
    G : networkx.MultiDiGraph
        input graph
    strongly : bool
        if True, return the largest strongly instead of weakly connected
        component

    Returns
    -------
    G : networkx.MultiDiGraph
        the largest connected component subgraph of the original graph
    """
    if strongly:
        kind = "strongly"
        is_connected = nx.is_strongly_connected
        connected_components = nx.strongly_connected_components
    else:
        kind = "weakly"
        is_connected = nx.is_weakly_connected
        connected_components = nx.weakly_connected_components

    if not is_connected(G):
        # get all the connected components in graph then identify the largest
        largest_cc = max(connected_components(G), key=len)
        n = len(G)

        # induce (frozen) subgraph then unfreeze it by making new MultiDiGraph
        G = nx.MultiDiGraph(G.subgraph(largest_cc))
        print(f"Got largest {kind} connected component ({len(G)} of {n} total nodes)")

    return G

def ts(style="datetime", template=None):
    """
    Get current timestamp as string.

    Parameters
    ----------
    style : string {"datetime", "date", "time"}
        format the timestamp with this built-in template
    template : string
        if not None, format the timestamp with this template instead of one of
        the built-in styles

    Returns
    -------
    ts : string
        the string timestamp
    """
    if template is None:
        if style == "datetime":
            template = "{:%Y-%m-%d %H:%M:%S}"
        elif style == "date":
            template = "{:%Y-%m-%d}"
        elif style == "time":
            template = "{:%H:%M:%S}"
        else:  # pragma: no cover
            raise ValueError(f"unrecognized timestamp style {style!r}")

    ts = template.format(dt.datetime.now())
    return ts

def _create_graph(response_jsons, retain_all=False, bidirectional=False):
    """
    Create a networkx MultiDiGraph from Overpass API responses.

    Adds length attributes in meters (great-circle distance between endpoints)
    to all of the graph's (pre-simplified, straight-line) edges via the
    `distance.add_edge_lengths` function.

    Parameters
    ----------
    response_jsons : list
        list of dicts of JSON responses from from the Overpass API
    retain_all : bool
        if True, return the entire graph even if it is not connected.
        otherwise, retain only the largest weakly connected component.
    bidirectional : bool
        if True, create bi-directional edges for one-way streets

    Returns
    -------
    G : networkx.MultiDiGraph
    """

    # make sure we got data back from the server request(s)
    if not any(rj["elements"] for rj in response_jsons):  # pragma: no cover
        raise EmptyOverpassResponse("There are no data elements in the response JSON")

    # create the graph as a MultiDiGraph and set its meta-attributes
    metadata = {
        "created_date": ts(),
        "created_with": f"OSMnx 1.0",
        "crs": 1.0,
    }
    G = nx.MultiDiGraph(**metadata)

    # extract nodes and paths from the downloaded osm data
    nodes = {}
    paths = {}
    for response_json in response_jsons:
        nodes_temp, paths_temp = _parse_nodes_paths(response_json)
        nodes.update(nodes_temp)
        paths.update(paths_temp)

    # add each osm node to the graph
    for node, data in nodes.items():
        G.add_node(node, **data)

    # add each osm way (ie, a path of edges) to the graph
    _add_paths(G, paths.values(), bidirectional)

    # retain only the largest connected component if retain_all is False
    if not retain_all:
        G = get_largest_component(G)

    # add length (great-circle distance between nodes) attribute to each edge
    if len(G.edges) > 0:
        G = add_edge_lengths(G)

    return G
    
if __name__ == "__main__":      

    filepath = '/mnt/3d1ece2f-6539-411b-bac2-589d57201626/home/burak/Downloads/osm/seychelles-latest.osm.bz2'
    #filepath = '/home/burak/Documents/repos/osmnx/tests/input_data/West-Oakland.osm.bz2'
    
    j = [_overpass_json_from_file(filepath)]

    G = _create_graph(j)

    

# coding=utf-8
import sys
import loader
import logging
from create import *

def connect_routes(sr, next_stops, start, end):
    sources = [start]
    count = 0
    while (count < 3): # dont go deep on neighbors for long
        a, b, c = None, None, None
        for source in sources:
            for x in sr[source]: # if source is                 
                if x in sr[end]: # on the same route as end point
                    return count, source, x # return the route
        next_sources = []
        for source in sources: # step one ahead
            for x in next_stops[source]:
                next_sources.append(x[0]) 
        sources = next_sources # and try again
        count += 1
    
    return None, None, None

def include_stop_counts(res, end, k): 
    newres = []
    for i in range(len(res)):
        toidx = 0
        fridx = 0
        if i != len(res)-1: # not last line
            fridx = K(k, res[i][2], res[i][0])
            toidx = K(k, res[i][2], res[i+1][0])
        if i == len(res)-1: # last line
            fridx = K(k, res[i][2], res[i][0])
            toidx = K(k, res[i][2], end)            
        res[i].append(abs(toidx - fridx))
        newres.append(res[i])

    return newres
        
def find(sr, k, next_stops, duraklar, start, end):
    count = 99999
    res = []
    tmp = []
    while (count > 0):
        count, stop, route = connect_routes(sr, next_stops, start, end)
        if count == None: return
        res.insert(0, [stop, duraklar[stop], route])
        end = stop

    res = include_stop_counts(res, end, k)

    return res

def find_using_hubs(sr, k, next_stops, duraklar, start, end):
    res = []
    
    # try common hubs
    f = open("hubstops.pkl")
    hubstops = pickle.load(f)
    f.close()    
                    
    for hub in hubstops:
        # try to reach hub
        to_hub = find(sr, k, next_stops, duraklar, start, hub)
        if to_hub != None: 
            # try to reach from hub to end
            from_hub = find(sr, k, next_stops, duraklar, hub, end)
            if from_hub != None:
                res = []
                for x in to_hub: res.append(x)
                for x in from_hub: res.append(x)
                return res                   
                
if __name__ == "__main__":        
    duraklar, next_stops, sr, routestops, k = loader.load()        
    #print connect_routes(sr, next_stops, 'L0168A','Þ0015L')          
    
    #print duraklar['L0168A']
    #print duraklar['Þ0015L']
    #res = find(sr, k, next_stops, duraklar, 'L0168A','Þ0015L')
    #res = find(sr, k, next_stops, duraklar, 'L0168A','Þ0015C')
    #res = find(sr, k, next_stops, duraklar, 'L0168A','Þ0015E')
    #res = find_using_hubs(sr, k, next_stops, duraklar, 'L0168A','L0114B')
    res = find_using_hubs(sr, k, next_stops, duraklar, 'T0037A','A1028A')
    print res

# coding=utf-8
import pickle
import sys

#durakfile = "data/testdurak1"
#hatfile = "data/testhat1"
durakfile = "data/durak"
hatfile = "data/hatdetay"

def replace_tr(str):
  str = str.replace("Ç","C")
  str = str.replace("Ý","I")
  str = str.replace("Þ","S")
  str = str.replace("Ð","G")      
  str = str.replace("Ü","U")      
  str = str.replace("Ö","O")  
  str = str.replace("þ","s")  
  str = str.replace("ö","o")  
  str = str.replace("ç","c")  
  str = str.replace("ý","i")  
  str = str.replace("ð","g")  
  str = str.replace("ü","u")  
  str = str.replace("\n","")    
  return str

def read_duraklar():
  file = open(durakfile)
  duraklar = {}
  for line in file.readlines():
    line = line.replace("\n","")
    line = line.replace(": ",":")
    line = line.replace(" :",":")
    tokens = line.split(":")
    if len(tokens) == 3:
      duraklar[tokens[0]] = replace_tr(tokens[1]) + " " + replace_tr(tokens[2])
      
  return duraklar

def read_sr():
    file = open(hatfile)
    sr = {}
    for line in file.readlines():
        tokens1 = line.split(",")
        route = tokens1[0]
        tokens2 = tokens1[1:]
        for i in range(len(tokens2)-1):
            stop = tokens2[i].split(':')[1]
            if stop not in sr.keys():
                sr[stop] = []
            sr[stop].append(route)
    return sr
            
def read_k():
    file = open(hatfile)
    k = {}
    routestops = {}
    for line in file.readlines():
        tokens1 = line.split(",") 
        route = tokens1[0]
        tokens2 = tokens1[1:]
        stops = []
        for i in range(len(tokens2)-1):
            stop = tokens2[i].split(':')[1]
            k[route + ":" + stop] = i+1
            stops.append(stop)
        routestops[route] = stops
    return routestops, k

def K(k, route, stop):
    if route+":"+stop not in k.keys():
        return 0
    else: 
        return k[route+":"+stop]


def read_next_stops():
    file = open(hatfile)
    next_stops = {}
    for line in file.readlines():
        tokens1 = line.split(",")  
        route = tokens1[0]
        tokens2 = tokens1[1:]
        for i in range(len(tokens2)-2):
            before = tokens2[i].split(':')[1]
            after = tokens2[i+1].split(':')[1]
            if before not in next_stops:
              next_stops[before] = []
            next_stops[before].append([after, route])
    return next_stops
  

def create():
  
    duraklar = read_duraklar()
    output = open('duraklar.pkl', 'wb')
    pickle.dump(duraklar, output)
    output.close()
        
    routestops, k = read_k()
    output = open('k.pkl', 'wb')
    pickle.dump(k, output)
    output.close()    
    output = open('routestops.pkl', 'wb')
    pickle.dump(routestops, output)
    output.close()
    
    next_stops = read_next_stops()
    output = open('nextstops.pkl', 'wb')
    pickle.dump(next_stops, output)
    output.close()
    
    sr = read_sr()
    output = open('sr.pkl', 'wb')
    pickle.dump(sr, output)
    output.close()
    
if __name__ == "__main__":
    create()

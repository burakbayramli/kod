# coding=utf-8
import pickle
import sys
  
def create():
  
    f = open("duraklar.pkl")
    duraklar = pickle.load(f)
    f.close()
    route_list = list(set(duraklar.values()))
    
    f = open('sr.pkl')
    sr = pickle.load(f)
    f.close()
    
    hubs = []
    hubstops = []
    for x in route_list:
      sum = 0
      stops = []
      for key in duraklar.keys():
        if x == duraklar[key]: 
            sum += len(sr[key])
            stops.append(key)
      hubs.append([x, sum, stops])
      hubstops.extend(stops)

    print len(hubs)
    hubs = [elem for elem in hubs if elem[1] > 70]
    print len(hubs)
    
    hubs.sort(lambda x,y: cmp(-x[1],-y[1]))
    #print hubs
    
    output = open('hubs.pkl', 'wb')
    pickle.dump(hubs, output)
    output.close()    
    
    output = open('hubstops.pkl', 'wb')
    pickle.dump(hubstops, output)
    output.close()    
    
    
if __name__ == "__main__":
    create()

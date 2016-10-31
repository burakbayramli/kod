import pickle

def load():

    f = open("duraklar.pkl")
    duraklar = pickle.load(f)
    f.close()
    
    f = open("nextstops.pkl")
    next_stops = pickle.load(f)
    f.close()
    
    f = open('sr.pkl')
    sr = pickle.load(f)
    f.close()

    f = open('k.pkl')
    k = pickle.load(f)
    f.close()

    f = open('routestops.pkl')
    routestops = pickle.load(f)
    f.close()    
    
    return duraklar, next_stops, sr, routestops, k

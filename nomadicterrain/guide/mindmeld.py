from datetime import datetime
from datetime import timedelta
import mapping, itertools, os
import pandas as pd
import numpy as np

fdir = os.path.dirname(os.path.realpath(__file__))
planets = ['sun','mo','mer','ven','mar','ju','sa','ur','ne','pl']

sun_moon_table = np.array(range(144)).reshape((12,12)) + 1

def get_decans(date):
   # Decans values are between 1 and 24, there are 10 of them in an
   # array per birthday. This data comes from SwissEph. Array cells
   # represent sun, moon, mercury, etc.  First one is sun, second is
   # the moon, the order is the same as the array shown in
   # mapping.planets. 
   decans = pd.read_csv(fdir + "/" + 'data/decans.dat',names=['date','decans'],sep=' ')
   tmp=np.array(decans[decans['date']==int(date)]['decans'])
   res = tmp[0].split(':')   
   res = res[:-1]
   res = list(map(int, res))
   return res

def calculate_millman(date):
    millman = []
    sum1 = 0; sum2 = 0
    for s in date: sum1+=int(s)
    for s in str(sum1): sum2+=int(s)
    millman.append(sum1)
    millman.append(sum2)
    for s in str(sum1)+str(sum2): millman.append(int(s))
    res = []
    res = [x for x in millman[2:] if x not in res]
    res.insert(0,millman[0])
    res.insert(1,millman[1])    
    return res

def get_spiller(date):
   spiller = pd.read_csv(fdir + "/" + "data/spiller",names=['from','to','sign'])
   res = spiller.apply(lambda x: int(date) >=int(x['from']) and int(date) <= int(x['to']),axis=1)
   if not np.any(res): return None
   return np.array(spiller[res])[0][2]
   
def get_chinese(date):
   chinese = pd.read_csv(fdir + "/" + "data/chinese",names=['from','to','sign'])
   res = chinese.apply(lambda x: int(date) >=int(x['from']) and int(date) <= int(x['to']),axis=1)
   if not np.any(res): return None
   return np.array(chinese[res])[0][2]
   
def calculate_cycle(d):
   try: 
       birth_date = datetime.strptime(d, '%Y%m%d').date()
       str_d = birth_date.strftime('%d %B %Y')
       now_year = datetime.now().year      
       cs = str(birth_date.day)+"/"+str(birth_date.month)+"/"+str(now_year)
       cycle_date = datetime.strptime(cs, '%d/%m/%Y').date()  
       str_cycle_date = cycle_date.strftime('%Y%m%d')
       millman = calculate_millman(str_cycle_date)
       res = str(millman[0])
       res = res[0:2]
       if len (res) > 1:
          total = int(res[0]) + int(res[1])
       else:
          total = int(res[0])
       if total > 9: 
           res = str(total)
           total = int(res[0]) + int(res[1])
       return total
   except: return None
   
def calculate_lewi_decans(decans):
   import pandas as pd
   smap = mapping.init()
   res = []
   # In order to map the 1-24 decan value to a sign, a little division
   # magic is used. Each sign has 3 decan values, 1-3 is Aries, 4-6 is
   # Taurus, etc. Below this mapping is done for sun and moon only.
   sun = np.ceil(float(decans[0])/3)-1
   moon = np.ceil(float(decans[1])/3)-1
   res.append(sun_moon_table[int(sun),int(moon)])

   # now calculate all the angles
   step_signs = ['*', 'sq', 'tri', 'opp', 'tri', 'sq', '*']
   steps = np.array([6,9,12,18,24,27,30])
   decans = np.array(decans)
   for planet in planets:
      decan = decans[planets.index(planet)]
      relpos = steps + decan; relpos = map(lambda x: x % 36,relpos)
      for pos,step_sign in zip(relpos,step_signs):
         matches = np.array(range(10))[decans == pos]
         pls = np.array(planets)[decans == pos]
         if len(matches)>0:
            for match,p in zip(matches,pls):
               if not pd.isnull(smap.loc[planet,step_sign]) and (p in smap.loc[planet,step_sign]):
                  res.append(smap.loc[planet,step_sign][p])

   # this part is for planet alignments, i.e. detecting same decans
   # that are for multiple planets.
   for i,dec in enumerate(decans):
      matches = np.array(range(10))[decans==dec]
      if len(matches) > 1:
         for x in matches:
            if i<x:
               if not pd.isnull(smap.loc[planets[i],'tick']) and (planets[x] in smap.loc[planets[i],'tick']):
                  res.append(smap.loc[planets[i],'tick'][planets[x]])
            
                  
   return sorted(res)


def calculate_lewi(date):
   return calculate_lewi_decans(get_decans(date))

def calculate(date):
   decans = get_decans(date)
   print (decans)
   sun = np.ceil(float(decans[0])/3)-1
   moon = np.ceil(float(decans[1])/3)-1
   diff = (datetime.now() - datetime.strptime(str(date), '%Y%m%d')).days
   return {
      'age': diff / 365,
      'chinese':get_chinese(date),
      'spiller':get_spiller(date), 'millman':calculate_millman(date),
      'cycle': calculate_cycle(date), 'lewi':calculate_lewi(date)}

def describe(res):
   print (res)
   base = './doc/details'
   print (base + '/millman/' + str(res['millman'][0]) + str(res['millman'][1]) + '.html:1:-')
   for lewi in res['lewi']:
      print (base + '/lewi/' + str(lewi) + '.html:1:-')
   print (base + '/chinese/' + str(res['chinese']) + '.html:1:-')
   print (base + '/spiller/' + str(res['spiller']) + '.html:1:-')

def conv(s):
    return datetime.strptime(s, '%d/%m/%Y').date().strftime('%Y%m%d')

def calculate_all_lewi():
   '''
   Calculates all lewi numbers for decans. Decans must have been calculated
   first using jlewi
   '''
   startd = '1/1/2020'
   endd = '1/1/2100'
   s = datetime.strptime(startd, '%d/%m/%Y')
   e = datetime.strptime(endd, '%d/%m/%Y')
   d = timedelta(days=1)
   while (s+d != e):
      date = s.strftime('%Y%m%d')
      print (date, calculate_lewi(date))
      s = s + d
   
if __name__ == "__main__": 
   calculate_all_lewi()

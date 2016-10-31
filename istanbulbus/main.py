#!/usr/bin/env python
import cgi
import logging
import wsgiref.handlers
from google.appengine.ext import webapp
from google.appengine.api import memcache
from django.utils import simplejson
import loader
from home import *
from create import *
from pathfinder import *

logging.getLogger().setLevel(logging.DEBUG)

duraklar, next_stops, sr, routestops, k = loader.load()        

route_list = list(set(duraklar.values()))

class Routes(webapp.RequestHandler):  
  def get(self):
    self.response.out.write(simplejson.dumps( route_list ))    
    
class CalculateRoute(webapp.RequestHandler):
  
  def get(self):
    fr = self.request.get('from')
    to = self.request.get('to')
    res_fr = []
    res_to = []
    for key in duraklar.keys():
      if fr == duraklar[key]: res_fr.append(key)
      if to == duraklar[key]: res_to.append(key)
               
    res = []
    
    # this did not work on GAE because it's Python is 2.5, 
    # we need >= 2.6
    #for (fr,to) in itertools.product(res_fr, res_to):
    combo = [(a, b) for a in res_fr for b in res_to]
    for (fr,to) in combo:
      x = find(sr, k, next_stops, duraklar, fr, to)      
      if x != None: res.append(x)
      
    if len(res) == 0:
      for (fr,to) in combo:
        x = find_using_hubs(sr, k, next_stops, duraklar, fr, to)
        if x != None: res.append(x)
                  
    self.response.out.write(simplejson.dumps(res))

application = webapp.WSGIApplication([
  ('/route_names', Routes),
  ('/calculate_route', CalculateRoute),
  ('/', Home),
], debug=True)

def main():
  wsgiref.handlers.CGIHandler().run(application)

if __name__ == '__main__':
  main()

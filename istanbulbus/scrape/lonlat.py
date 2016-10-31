# coding=utf-8

import os,urllib,re

def get_longlat(addr):
    
    url = ''
    if addr[0]=='(':
        center = addr.replace('(','').replace(')','')
        lat,lng = center.split(',')
        url = 'http://maps.google.com/maps?q=%s+%s' % (lat,lng)
    else:
        # Encode query string into URL
        url = 'http://maps.google.com/?q=' + urllib.quote(addr) + '&output=js'
        print '\nQuery: %s' % (url)
    
        # Get XML location
        xml = urllib.urlopen(url).read()
    
        if '<error>' in xml:
           print '\nGoogle cannot interpret the address.'
        else:
            # Strip lat/long coordinates from XML
            lat,lng = 0.0,0.0
            center = xml[xml.find('{center')+10:xml.find('}',xml.find('{center'))]
            center = center.replace('lat:','').replace('lng:','')
            lat,lng = center.split(',')
            url = 'http://maps.google.com/maps?q=%s+%s' % (lat,lng)

    if url<>'':
        return url
    
if __name__ == "__main__":
    #print get_longlat("yanarsu sok istanbul turkey")
    #print get_longlat("iett taksim duragi istanbul turkey")
  
    outfile = open ("../../res/raw/lonlat", "w")
    
    infile = open("../../res/raw/hatdetaydurakutf")
    for line in infile.readlines():
        try: 
            tokens = line.split(":")
            durak = tokens[0]
            durakname = tokens[1] + " " + tokens[2]
            str = "iett " + durakname + " istanbul"
            res = get_longlat(str)
            print res
            if "-95" not in res:
                found = re.search("(\d*?\.\d*?)\+(\d*?\.\d*)", res).groups()
                print found
                outfile.write(durak + ":" + found[0] + ":" + found[1])
                outfile.write("\n")
                outfile.flush()
        except Exception, e:
            print e
            
    fileout.close()

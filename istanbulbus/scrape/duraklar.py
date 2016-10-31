# coding=utf-8
import urllib
import re
import os

out = open ("/tmp/duraklar", "w")

for letter in ['A', 'B', 'C', '%C7', 'D', 'E', 'F', 
               'G', 'H', 'I', '%DD', 'J', 'K', 'L', 
               'M', 'N', 'O', '%D6', 'P', 'R', 'S', 
               '%DE', 'T', 'U', '%DC', 'V', 'Y', 'Z']:
    print "harf: " + letter
    url = "http://www.iett.gov.tr/saat/orer.php?hid=durak&Letter=" + letter
    h = urllib.urlopen(url)
    for line in h.readlines():
        regex = "<a href=orer.php\?hid=durakhat\&durak=(.*?)\&durakname=(.*?)>"
        res = re.findall(regex, line)
        for cell in res: 
            out.write(cell[0] + ":" + cell[1] + "\n")
            out.flush()
                        
out.close()

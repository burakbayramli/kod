# coding=utf-8
# gets unique list of duraks from hatlar. 
import urllib
import re
import os

infile = open("/tmp/hatlar")
outfile = open("/tmp/hatdetaydurak", "w")

duraks = {}

for hat in infile.readlines():
    try:
        hat = hat.replace("\n","")
        url3 = "http://www.iett.gov.tr/saat/orer.php?hid=hat&hatcode=" + hat + "&BtnDetay.x=4"
        h3 = urllib.urlopen(url3)
        #outfile.write(hat + ":")
        regex3 = "<td class=text1> (\d*) </td><td>(.*?)<font color=gray>(.*?)<font>(.*?)durak_kodu=(.*?)\&"
        res3 = re.findall(regex3, h3.read())   
        if len(res3) > 0: 
            for durak in res3:
                if (durak[4] not in duraks):
                    duraks[durak[4]] = True
                    outfile.write(durak[4] + ":" + durak[1] + ":" + durak[2])
                    outfile.write("\n")
                #print durak[1], durak[2], durak[4]
            outfile.flush()
            outfile.write("\n")
                
    except Exception, e:
        print e
        print "hat yanlis\n"
    
infile.close()
outfile.close()

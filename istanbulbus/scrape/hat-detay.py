# coding=utf-8
import urllib
import re
import os

infile = open("/tmp/hatlar")
outfile = open("/tmp/hatdetay", "w")


for hat in infile.readlines():
    try:
        hat = hat.replace("\n","")
        url3 = "http://www.iett.gov.tr/saat/orer.php?hid=hat&hatcode=" + hat + "&BtnDetay.x=4"
        h3 = urllib.urlopen(url3)
        outfile.write(hat + ":")
        regex3 = "<td class=text1> (\d*) </td><td>(.*?)<font color=gray>(.*?)durak_kodu=(.*?)\&"
        res3 = re.findall(regex3, h3.read())   
        if len(res3) > 0: 
            for durak in res3:
                outfile.write(durak[0] + "," + durak[3] + ",")                    
            outfile.flush()
            outfile.write("\n")

                
    except Exception, e:
        print e
        print "hat yanlis\n"
    
infile.close()
outfile.close()

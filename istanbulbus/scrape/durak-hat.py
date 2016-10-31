# coding=utf-8
import urllib
import re
import os

outfile = open ("/tmp/durakhat", "w")

infile = open("/tmp/duraklar")
for line in infile.readlines():
    durak = line.split(":")[0]
    url2 = "http://www.iett.gov.tr/saat/orer.php?hid=durakhat&durak=" + durak
    h2 = urllib.urlopen(url2)
    outfile.write(durak + ":")
    for line2 in h2.readlines():
        regex2 = "<a href=orer.php\?hid=hat\&hatcode=(.*?)\&durak=(.*?)>"
        res2 = re.findall(regex2, line2)
        for hat in res2:
            outfile.write(hat[0] + ":")
    outfile.write("\n")
    outfile.flush()

outfile.close()

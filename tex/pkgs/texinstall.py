
'''Assuming all .sty files to be installed are in the same folder as
this script, creates an ls-R file and creates directories necessary
under /usr/local/share/texmf and copies files to the right places
'''
import os

pkgs = ['minted','showkeys','ifplatform','eulervm','etoolbox','cancel',
        'xstring','lineno','marginnote','algorithm','algorithmic','pseudocode']

fout = open("/tmp/ls-R","w")
fout.write("% ls-R -- filename database for kpathsea; do not change this line.")
fout.write("\n")
fout.write("./tex/latex:\n")
for x in pkgs: fout.write(x+"\n")
    
fout.write("\n")

for x in pkgs:
    fout.write("./tex/latex/%s:\n" % x)
    fout.write("%s.sty\n" % x)
    fout.write("%s.tex\n" % x)

for x in pkgs:
    os.system("mkdir /usr/local/share/texmf/tex/latex/%s" % x )
    os.system("cp %s.sty /usr/local/share/texmf/tex/latex/%s/" % (x,x) )
    os.system("cp %s.tex /usr/local/share/texmf/tex/latex/%s/" % (x,x) )

fout.close()

os.system("cp /tmp/ls-R /usr/local/share/texmf/" )
    
 

import subprocess, re, sys

p = subprocess.Popen(['recoll','-t',sys.argv[1]], stdout=subprocess.PIPE)
for line in p.stdout:
    res = re.findall('\[file:\/\/(.*?)\]', str(line), re.DOTALL)
    print (res)
    if (len(res)>0):
        print ("%s:1:1" % res[0])

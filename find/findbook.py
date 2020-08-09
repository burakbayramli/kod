import subprocess, re, sys

p = subprocess.Popen(['recoll','-t',sys.argv[1]], stdout=subprocess.PIPE)
for line in p.stdout:
    #print (line)
    res = re.findall('\[file:\/\/(.*?)\]', str(line), re.DOTALL)
    if (len(res)>0):
        print (res)
        print ("%s:%d:%s" % (res[0], 0, 0))

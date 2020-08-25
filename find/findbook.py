import subprocess, re, sys

p = subprocess.Popen(['recoll','-t',sys.argv[1]], stdout=subprocess.PIPE)
for line in p.stdout:
    res = re.findall('\[file:\/\/(.*?)\]', str(line), re.DOTALL)
    if (len(res)>0):
        print (":3:%s:308::" % res[0])

import os
ps = ['Asclepias','Abelia grandiflora']
for p in ps:
    print (p)
    done_file = "dataset/bing/%s/%s.done" % (p,p)
    if not os.path.isfile(done_file): 
        os.system("python -u console.py bing '%s' --limit 50 --json" % p)
        fout = open(done_file, "w")
        fout.write('done')
        fout.close()        
    else:
        print ('already downloaded...skipping')        





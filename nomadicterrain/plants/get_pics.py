import os, console
ps = ['Abutilon abutiloides','Asclepias','Abelia grandiflora']
for p in ps:
    print (p)
    done_file = "dataset/bing/%s/%s.done" % (p,p)
    if not os.path.isfile(done_file): 
        console.run_query(p, 50)
        fout = open(done_file, "w")
        fout.write('done')
        fout.close()        
    else:
        print ('already downloaded...skipping')        





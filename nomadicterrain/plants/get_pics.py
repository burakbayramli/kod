import pandas as pd
import os, console

ps = ['Abutilon abutiloides','Asclepias','Abelia grandiflora']

dfp = pd.read_csv('/home/burak/Downloads/campdata/edible_plants.csv',sep='|')
dfp_list = list(dfp['Scientific Name'])

df = pd.read_csv('/home/burak/Downloads/campdata/europe_plants',sep='|')
#print (list(df['sciname']))
#exit()
ps = list(df['sciname'])
for p in ps:
    if p not in dfp_list: continue
    print (p)    
    done_file = "dataset/bing/%s/%s.done" % (p,p)
    if not os.path.isfile(done_file):
        try:
            console.run_query(p, 200)
            fout = open(done_file, "w")
            fout.write('done')
            fout.close()
        except Exception as e:
            print (repr(e))
            continue
    else:
        print ('already downloaded...skipping')        





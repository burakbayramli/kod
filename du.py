"""Works kind of like Unix du - lists files along with their sizes,
and sorts then in a descending manner, writes the output under TEMP
you can also do
sudo du -hx --max-depth=1 /
"""
import sys, glob, os, shutil, pandas as pd

def ls(d):
    files = []
    for root, directories, filenames in os.walk(d):
        for filename in filenames:
            path = os.path.join(root,filename)
            try: 
                files.append((path, os.path.getsize(path)))
            except Exception:
                print ('error')
                pass
    return files

    
if __name__ == "__main__":
    res = ls(sys.argv[1])
    tmp = '/tmp'
    if 'TEMP' in os.environ: tmp = os.environ['TEMP']
    df = pd.DataFrame(res,columns=['name','size'])
    df = df.sort_values(by='size',ascending=False)
    print (df[['size','name']].head(30))
    df[['size','name']].head(100).to_csv('%s/du.csv' % tmp,index=None)
    print ("\nMore detailed output is under %s/du.csv" % tmp)
    

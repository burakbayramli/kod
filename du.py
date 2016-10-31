"""Works kind of like Unix du - lists files along with their sizes,
and sorts then in a descending manner, writes the output under TEMP

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
                print 'error'
                pass
    return files

    
if __name__ == "__main__":
    res = ls(sys.argv[1])
    df = pd.DataFrame(res,columns=['name','size'])
    df = df.sort('size',ascending=False)
    print df[['size','name']].head(30)
    df[['size','name']].head(100).to_csv('%s/du.csv' % os.environ['TEMP'],index=None)
    print "\nMore detailed output is under %s/du.csv" % os.environ['TEMP']
    

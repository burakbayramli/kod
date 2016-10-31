# Rsync written in Python: argument from_folder is assumed to be
# the master, its contents are reflected in to_folder. If --delete
# flag is used extra files under to_folder will be deleted after
# copy sync is done, so in this case from_folder will be reflected
# *exactly* under to_folder. For file equality file size is used,
# if same file name with same file size exists under to_folder, it
# will not be copied again.

# python rsync.py c:\Users\burak\Documents\a c:\Users\burak\Documents\b

import sys, glob, os, shutil, re

def deleteDir(path):
    """deletes the path entirely"""
    mswindows = (sys.platform == "win32")
    if mswindows: 
        cmd = "RMDIR "+ path +" /s /q"
    else:
        cmd = "rm -rf "+path
    result = getstatusoutput(cmd)
    if(result[0]!=0):
        raise RuntimeError(result[1])

def deleteFile(path):
    """deletes the path entirely"""
    mswindows = (sys.platform == "win32")
    if mswindows: 
        cmd = 'DEL /F /S /Q "%s"' % path
    else:
        cmd = "rm -rf " + path
    result = getstatusoutput(cmd)
    if(result[0]!=0):
        raise RuntimeError(result[1])

def getstatusoutput(cmd):
    """Return (status, output) of executing cmd in a shell."""
    mswindows = (sys.platform == "win32")
    if not mswindows:
        return commands.getstatusoutput(cmd)
    pipe = os.popen(cmd + ' 2>&1', 'r')
    text = pipe.read()
    sts = pipe.close()
    if sts is None: sts = 0
    if text[-1:] == '\n': text = text[:-1]
    return sts, text
        
def ls(d):
    dirs = []; files = []
    for root, directories, filenames in os.walk(d):
        for directory in directories:
            path = os.path.join(root, directory)
            if ".git" not in path: dirs.append(path)
        for filename in filenames: 
            path = os.path.join(root,filename)
            if ".git" not in path: files.append((path, os.path.getsize(path)))
    return dirs, files

def purge(dir, pattern, inclusive=True):
    regexObj = re.compile(pattern)
    for root, dirs, files in os.walk(dir, topdown=False):
        for name in files:
            path = os.path.join(root, name)
            if bool(regexObj.search(path)) == bool(inclusive):
                os.remove(path)
                
def copy_files_and_dirs(fr,to):    
    frdirs,frfiles =  ls(fr)    
    todirs,tofiles = ls(to)
    tofilesdict = dict(tofiles)
    
    print 'create dirs'
    todirs_tmp = dict([(x.replace(fr,to),0) for x in todirs])
    diff = [x for x in frdirs if x.replace(fr,to) not in todirs_tmp]
    for x in diff:
        x=x.replace(fr,to)
        os.mkdir(x)

    print 'a files not in b'
    for (x,size) in frfiles:
        x_to=x.replace(fr,to)
        if x_to in tofilesdict and tofilesdict[x_to] != size: 
            print 'copying', x,x_to
            shutil.copy(x,x_to)
        elif x_to not in tofilesdict: 
            print 'copying', x,x_to
            shutil.copy(x,x_to)
            
    return frdirs, todirs

def del_not_in_from(fr, to, frdirs, todirs):
    print 'b files not in a'
    frdirs_tmp = dict([(x.replace(to,fr),0) for x in frdirs])
    diff = [x for x in todirs if x.replace(to,fr) not in frdirs_tmp]
    for x in diff:
        print 'deleting directory', x
        if os.path.isdir(x): deleteDir(x)

    frdirs,frfiles =  ls(fr)
    todirs,tofiles = ls(to)
    frfilesdict = dict(frfiles)
    
    for (x,size) in tofiles:
        x_fr=x.replace(to,fr)
        if x_fr not in frfilesdict:
            print 'deleting', x
            deleteFile(x)

            
if __name__ == "__main__":
    
    if len(sys.argv) < 3:
        print "Usage: rsync.py from_folder to_folder [--delete]"
        exit()
    fr = sys.argv[1]
    to = sys.argv[2]
    is_delete = False
    if len(sys.argv) == 4 and sys.argv[3]=='--delete': is_delete = True
    print fr, to
    frdirs, todirs = copy_files_and_dirs(fr,to)
    if is_delete: del_not_in_from(fr, to, frdirs, todirs)
    


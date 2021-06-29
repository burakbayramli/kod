import os, sys, rsync, glob

if len(sys.argv) < 2:
    print ("Usage dsync.py [letter]")
    exit()

if sys.argv[1] == "acer_ext":
    tmp = glob.glob('/home/burak/Documents/kitaplar/*')
    if (len(tmp)==0):
        print ('\n============= PARTITION NOT MOUNTED =================')
        exit()
    os.system("python rsync.py '/home/burak/Documents/Dropbox' '/media/burak/Seagate Backup Plus Drive/archive/Dropbox' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/thirdwave' '/media/burak/Seagate Backup Plus Drive/archive/repos/thirdwave'  --ignore-list=.git --delete 1")
    os.system("python rsync.py '/home/burak/Documents/kod' '/media/burak/Seagate Backup Plus Drive/archive/kod' --ignore-list=.git --delete 1")
    os.system("python rsync.py '/home/burak/Documents/classnotes' '/media/burak/Seagate Backup Plus Drive/archive/classnotes'  --ignore-list=.git  --delete 1")
    os.system("python rsync.py '/home/burak/Documents/books' '/media/burak/Seagate Backup Plus Drive/archive/repos/books'  --ignore-list=.git")
    os.system("python rsync.py '/home/burak/Documents/kitaplar' '/media/burak/Seagate Backup Plus Drive/archive/kitaplar'  --delete 1")
    #os.system("python rsync.py '/home/burak/Downloads/campdata' '/media/burak/Seagate Backup Plus Drive/archive/data/campdata'  --delete 1")
    
if sys.argv[1] == "ext1_ext2":
    os.system("python rsync.py '/media/burak/Seagate Backup Plus Drive/shows' '/media/burak/Backup Plus/shows'  --delete 1")
    os.system("python rsync.py '/media/burak/Seagate Backup Plus Drive/archive/Dropbox' '/media/burak/Backup Plus/archive/Dropbox'  --delete 1")
    os.system("python rsync.py '/media/burak/Seagate Backup Plus Drive/archive/data' '/media/burak/Backup Plus/archive/data'  --delete 1")
    os.system("python rsync.py '/media/burak/Seagate Backup Plus Drive/archive/kitaplar' '/media/burak/Backup Plus/archive/kitaplar' --delete 1")
    os.system("python rsync.py '/media/burak/Seagate Backup Plus Drive/Lectures' '/media/burak/Backup Plus/Lectures' ")
    
if sys.argv[1] == "acer_usb64":
    tmp = glob.glob('/home/burak/Documents/kitaplar/*')
    if (len(tmp)==0):
        print ('\n============= PARTITION NOT MOUNTED =================')
        exit()
    os.system("python rsync.py '/home/burak/Documents/Dropbox' '/media/burak/1BC3-0618/archive/Dropbox' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/thirdwave' '/media/burak/1BC3-0618/archive/repos/thirdwave'  --ignore-list=.git  --delete 1")
    os.system("python rsync.py '/home/burak/Documents/kod' '/media/burak/1BC3-0618/archive/kod' --ignore-list=.git  --delete 1")
    os.system("python rsync.py '/home/burak/Documents/classnotes' '/media/burak/1BC3-0618/archive/classnotes'  --ignore-list=.git  --delete 1")
    os.system("python rsync.py '/home/burak/Documents/kitaplar' '/media/burak/1BC3-0618/archive/kitaplar'  --delete 1")
                
    
    

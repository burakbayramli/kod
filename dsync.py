import os, sys, rsync, glob

if len(sys.argv) < 2:
    print ("Usage dsync.py [letter]")
    exit()

if sys.argv[1] == "acer_ext":
    tmp = glob.glob('/home/burak/Documents/kitaplar/*')
    if (len(tmp)==0):
        print ('\n============= PARTITION NOT MOUNTED =================')
        exit()
    os.system("python rsync.py '/home/burak/Documents/Dropbox' '/media/burak/Backup Plus/Dropbox' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/thirdwave' '/media/burak/Backup Plus/archive/repos/thirdwave'  --ignore-list=.git --delete 1")
    os.system("python rsync.py '/home/burak/Documents/kod' '/media/burak/Backup Plus/archive/kod' --ignore-list=.git --delete 1")
    os.system("python rsync.py '/home/burak/Documents/classnotes' '/media/burak/Backup Plus/archive/classnotes'  --ignore-list=.git  --delete 1")
    #os.system("python rsync.py '/home/burak/Documents/books' '/media/burak/Seagate Backup Plus Drive/archive/repos/books'  --ignore-list=.git")
    os.system("python rsync.py '/home/burak/Documents/kitaplar' '/media/burak/Backup Plus/archive/kitaplar'  --delete 1")
    
if sys.argv[1] == "ext1_ext2":
    os.system("python rsync.py '/media/burak/Backup Plus/shows' '/media/burak/Seagate Backup Plus Drive/shows'   --delete 1")
    os.system("python rsync.py '/media/burak/Backup Plus/archive/Dropbox' '/media/burak/Seagate Backup Plus Drive/archive/Dropbox'  --delete 1")
    os.system("python rsync.py '/media/burak/Backup Plus/archive/data' '/media/burak/Seagate Backup Plus Drive/archive/data'   --delete 1")
    os.system("python rsync.py '/media/burak/Backup Plus/archive/kitaplar' '/media/burak/Seagate Backup Plus Drive/archive/kitaplar'  --delete 1")
    os.system("python rsync.py '/media/burak/Backup Plus/Lectures' '/media/burak/Seagate Backup Plus Drive/Lectures'  ")
    
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
                
    
    

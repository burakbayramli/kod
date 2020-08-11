import os, sys, rsync

if len(sys.argv) < 2:
    print ("Usage dsync.py [letter]")
    exit()

if sys.argv[1] == "acer_ext":
    os.system("python rsync.py '/home/burak/Documents/Dropbox' '/media/burak/Seagate Backup Plus Drive/archive/Dropbox' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/thirdwave' '/media/burak/Seagate Backup Plus Drive/archive/repos/thirdwave'  --ignore-list=.git --delete 1")
    os.system("python rsync.py '/home/burak/Documents/kod' '/media/burak/Seagate Backup Plus Drive/archive/kod' --ignore-list=.git --delete 1")
    os.system("python rsync.py '/home/burak/Documents/classnotes' '/media/burak/Seagate Backup Plus Drive/archive/classnotes'  --ignore-list=.git  --delete 1")
    os.system("python rsync.py '/home/burak/Documents/books' '/media/burak/Seagate Backup Plus Drive/archive/repos/books'  --ignore-list=.git")
    os.system("python rsync.py '/home/burak/Documents/kitaplar' '/media/burak/Seagate Backup Plus Drive/archive/kitaplar'  --delete 1")
    os.system("python rsync.py '/home/burak/Downloads/campdata' '/media/burak/Seagate Backup Plus Drive/archive/data/campdata'  --delete 1")
    
if sys.argv[1] == "acer_usb64":
    os.system("python rsync.py '/home/burak/Documents/Dropbox' '/media/burak/1BC3-0618/archive/Dropbox' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/thirdwave' '/media/burak/1BC3-0618/archive/repos/thirdwave'  --ignore-list=.git  --delete 1")
    os.system("python rsync.py '/home/burak/Documents/kod' '/media/burak/1BC3-0618/archive/kod' --ignore-list=.git  --delete 1")
    os.system("python rsync.py '/home/burak/Documents/classnotes' '/media/burak/1BC3-0618/archive/classnotes'  --ignore-list=.git  --delete 1")
    os.system("python rsync.py '/home/burak/Documents/books' '/media/burak/1BC3-0618/archive/books'  --ignore-list=.git  --delete 1")
    os.system("python rsync.py '/home/burak/Documents/kitaplar' '/media/burak/1BC3-0618/archive/kitaplar'  --delete 1")
    os.system("python rsync.py '/home/burak/.recoll' '/media/burak/1BC3-0618/archive/dotrecoll' ")
                
if sys.argv[1] == "acer_flashred":
    os.system("python rsync.py '/home/burak/Documents/Dropbox' '/media/burak/7502-42E6/Dropbox' --delete 1")
    
    

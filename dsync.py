import os, sys, shutil, rsync, re

if len(sys.argv) < 2:
    print ("Usage dsync.py [letter]")
    exit()

if sys.argv[1] == "acer_hd":
    os.system("python rsync.py '/home/burak/Documents/Dropbox' '/media/burak/Seagate Backup Plus Drive/archive/Dropbox' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/thirdwave' '/media/burak/Seagate Backup Plus Drive/archive/repos/thirdwave'  --ignore-list=.git --delete 1")
    os.system("python rsync.py '/home/burak/Documents/kod' '/media/burak/Seagate Backup Plus Drive/archive/kod' --ignore-list=.git --delete 1")
    os.system("python rsync.py '/home/burak/Documents/classnotes' '/media/burak/Seagate Backup Plus Drive/archive/classnotes'  --ignore-list=.git  --delete 1")
    
if sys.argv[1] == "acer_usb64":
    os.system("python rsync.py '/home/burak/Documents/Dropbox' '/media/burak/1BC3-0618/archive/Dropbox' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/thirdwave' '/media/burak/1BC3-0618/archive/repos/thirdwave'  --ignore-list=.git  --delete 1")
    os.system("python rsync.py '/home/burak/Documents/kod' '/media/burak/1BC3-0618/archive/kod' --ignore-list=.git  --delete 1")
    os.system("python rsync.py '/home/burak/Documents/classnotes' '/media/burak/1BC3-0618/archive/classnotes'  --ignore-list=.git  --delete 1")
    
if sys.argv[1] == "usb64_hd":
    os.system("python rsync.py '/media/burak/1BC3-0618/archive/kitaplar' '/media/burak/Seagate Backup Plus Drive/archive/kitaplar'  --delete 1")
    
if sys.argv[1] == "usb64_flashblue":
    os.system("python rsync.py '/media/burak/1BC3-0618/archive/kitaplar' '/media/burak/23B9-71E6/kitaplar'  --delete 1")
        
if sys.argv[1] == "acer_flash":
    #os.system("python rsync.py '/home/burak/Documents/kod' '/media/burak/7502-42E6/kod' --delete 1")
    #os.system("python rsync.py '/home/burak/Documents/classnotes' '/media/burak/7502-42E6/classnotes' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/Dropbox' '/media/burak/7502-42E6/Dropbox' --delete 1")
    
if sys.argv[1] == "hd_flashblue":
    os.system("python rsync.py '/media/burak/Seagate Backup Plus Drive/archive/kitaplar' '/media/burak/23B9-71E6/kitaplar' --delete 1")
    

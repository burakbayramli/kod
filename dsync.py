import os, sys, shutil, rsync, re

if len(sys.argv) < 2:
    print ("Usage dsync.py [letter]")
    exit()

if sys.argv[1] == "hd":
    os.system("python rsync.py '/home/burak/Documents/Dropbox' '/media/burak/Seagate Backup Plus Drive/archive/Dropbox' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/kod' '/media/burak/Seagate Backup Plus Drive/archive/kod' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/classnotes' '/media/burak/Seagate Backup Plus Drive/archive/classnotes' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/backlog' '/media/burak/Seagate Backup Plus Drive/archive/backlog' --delete 1")

if sys.argv[1] == "acer_usb64":
    os.system("python rsync.py '/home/burak/Documents/Dropbox' '/media/burak/1BC3-0618/archive/Dropbox' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/kod' '/media/burak/1BC3-0618/archive/kod' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/classnotes' '/media/burak/1BC3-0618/archive/classnotes' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/backlog' '/media/burak/1BC3-0618/archive/backlog' --delete 1")
    
if sys.argv[1] == "seagate_usb64":
    os.system("python rsync.py '/media/burak/Seagate Backup Plus Drive/archive' '/media/burak/1BC3-0618/archive' --delete 1")
    os.system("python rsync.py '/media/burak/Seagate Backup Plus Drive/kitaplar' '/media/burak/1BC3-0618/kitaplar' --delete 1")
    
if sys.argv[1] == "flash":
    os.system("python rsync.py '/media/burak/Seagate Backup Plus Drive/archive/kod' '/media/burak/7502-42E6/kod' --delete 1")
    os.system("python rsync.py '/media/burak/Seagate Backup Plus Drive/archive/classnotes' '/media/burak/7502-42E6/classnotes' --delete 1")
    os.system("python rsync.py '/media/burak/Seagate Backup Plus Drive/archive/books' '/media/burak/7502-42E6/books' --delete 1")
    
if sys.argv[1] == "acer_flash":
    os.system("python rsync.py '/home/burak/Documents/kod' '/media/burak/7502-42E6/kod' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/classnotes' '/media/burak/7502-42E6/classnotes' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/Dropbox' '/media/burak/7502-42E6/Dropbox' --delete 1")
    
if sys.argv[1] == "kitaplar":
    os.system("python rsync.py '/media/burak/Seagate Backup Plus Drive/archive/kitaplar' '/media/burak/23B9-71E6/kitaplar' --delete 1")
    
if sys.argv[1] == "sams60gb":
    pass
    #/storage/1BC3-0618

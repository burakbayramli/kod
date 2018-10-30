import os, sys, shutil, rsync, re

if len(sys.argv) < 2:
    print ("Usage dsync.py [letter]")
    exit()

if sys.argv[1] == "hd":
    os.system("python rsync.py '/home/burak/Documents/Dropbox' '/media/burak/Seagate Backup Plus Drive/archive/Dropbox' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/kod' '/media/burak/Seagate Backup Plus Drive/archive/kod' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/classnotes' '/media/burak/Seagate Backup Plus Drive/archive/classnotes' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/backlog' '/media/burak/Seagate Backup Plus Drive/archive/backlog' --delete 1")

if sys.argv[1] == "hd2usb64":
    os.system("python rsync.py '/media/burak/Seagate Backup Plus Drive/archive' '/media/burak/1BC3-0618/archive' --delete 1")
    os.system("python rsync.py '/media/burak/Seagate Backup Plus Drive/kitaplar' '/media/burak/1BC3-0618/kitaplar' --delete 1")
    
if sys.argv[1] == "flash":
    os.system("python rsync.py '/home/burak/Documents/kod' '/media/burak/7502-42E6/kod' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/classnotes' '/media/burak/7502-42E6/classnotes' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/books' '/media/burak/7502-42E6/books' --delete 1")
    




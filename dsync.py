import os, sys, shutil, rsync, re

if len(sys.argv) < 2:
    print ("Usage dsync.py [letter]")
    exit()

#if sys.argv[1] == "hd":
#    os.system("python rsync.py '/home/burak/Documents/Dropbox' '/media/burak/New Volume/archive/Dropbox' --delete 1")
#    os.system("python rsync.py '/home/burak/Documents/kod' '/media/burak/New Volume/archive/kod' --delete 1")
#    os.system("python rsync.py '/home/burak/Documents/classnotes' '/media/burak/New Volume/archive/classnotes' --delete 1")
#    os.system("python rsync.py '/home/burak/Documents/books' '/media/burak/New Volume/archive/books' --delete 1")
#    os.system("python rsync.py '/home/burak/Documents/backlog' '/media/burak/New Volume/archive/backlog' --delete 1")

if sys.argv[1] == "hd":
    os.system("python rsync.py '/home/burak/Documents/Dropbox' '/media/burak/Seagate Backup Plus Drive/archive/Dropbox' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/kod' '/media/burak/Seagate Backup Plus Drive/archive/kod' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/classnotes' '/media/burak/Seagate Backup Plus Drive/archive/classnotes' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/backlog' '/media/burak/Seagate Backup Plus Drive/archive/backlog' --delete 1")

if sys.argv[1] == "kitaplar":
    os.system("python rsync.py  '/media/burak/New Volume/archive/kitaplar' /media/burak/23B9-71E6/kitaplar  --delete 1")

if sys.argv[1] == "flash":
    os.system("python rsync.py '/home/burak/Documents/kod' '/media/burak/7502-42E6/kod' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/classnotes' '/media/burak/7502-42E6/classnotes' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/books' '/media/burak/7502-42E6/books' --delete 1")
    
if sys.argv[1] == "hd2hd":
    os.system("python rsync.py '/media/burak/New Volume/archive' '/media/burak/Seagate Backup Plus Drive/archive' --delete 1")
    os.system("python rsync.py '/media/burak/New Volume/shows' '/media/burak/Seagate Backup Plus Drive/shows' --delete 1")
    os.system("python rsync.py '/media/burak/New Volume/Lectures' '/media/burak/Seagate Backup Plus Drive/Lectures' --delete 1")
#    os.system("python rsync.py '/media/burak/New Volume/other' '/media/burak/Seagate Backup Plus Drive/other' --delete 1")

if sys.argv[1] == "hd2usb64":
    os.system("python rsync.py '/media/burak/Seagate Backup Plus Drive/Lectures/YT' '/media/burak/1BC3-0618/Lectures/YT' --delete 1")



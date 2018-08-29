import os, sys, shutil, rsync, re

if len(sys.argv) < 2: print "Usage dsync.py [letter]"; exit()

if sys.argv[1] == "hd":
    os.system("python rsync.py '/home/burak/Documents/Dropbox' '/media/burak/New Volume/archive/Dropbox' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/kod' '/media/burak/New Volume/archive/kod' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/classnotes' '/media/burak/New Volume/archive/classnotes' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/books' '/media/burak/New Volume/archive/books' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/backlog' '/media/burak/New Volume/archive/backlog' --delete 1")

if sys.argv[1] == "kitaplar":
    os.system("python rsync.py  '/media/burak/New Volume/archive/kitaplar' /media/burak/23B9-71E6/kitaplar  --delete 1")

if sys.argv[1] == "flash":
    os.system("python rsync.py '/home/burak/Documents/kod' '/media/burak/7502-42E6/kod' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/classnotes' '/media/burak/7502-42E6/classnotes' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/books' '/media/burak/7502-42E6/books' --delete 1")
    
if sys.argv[1] == "hd2hd":
    os.system("python rsync.py '/media/burak/New Volume/archive/Dropbox' '/media/burak/17EA-3758/archive/Dropbox' --delete 1")
    os.system("python rsync.py '/media/burak/New Volume/archive/kitaplar' '/media/burak/17EA-3758/archive/kitaplar' --delete 1")
    os.system("python rsync.py '/media/burak/New Volume/archive/kod' '/media/burak/17EA-3758/archive/kod' --delete 1")
    os.system("python rsync.py '/media/burak/New Volume/archive/classnotes' '/media/burak/17EA-3758/archive/classnotes' --delete 1")
    os.system("python rsync.py '/media/burak/New Volume/archive/books' '/media/burak/17EA-3758/archive/books' --delete 1")
    os.system("python rsync.py '/media/burak/New Volume/archive/data' '/media/burak/17EA-3758/archive/data' --delete 1")


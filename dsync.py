import os, sys, shutil, rsync, re

if len(sys.argv) < 2: print "Usage dsync.py [letter]"; exit()

if sys.argv[1] == "hd":
#    os.system("python rsync.py '/home/burak/Documents/bass' '/media/burak/New Volume/archive/bass' --delete")
    os.system("python rsync.py '/home/burak/Documents/Dropbox' '/media/burak/New Volume/archive/Dropbox' --delete")
    os.system("python rsync.py '/home/burak/Documents/kod' '/media/burak/New Volume/archive/kod' --delete")
    os.system("python rsync.py '/home/burak/Documents/classnotes' '/media/burak/New Volume/archive/classnotes' --delete")
    os.system("python rsync.py '/home/burak/Documents/books' '/media/burak/New Volume/archive/books' --delete")
    os.system("python rsync.py '/home/burak/Documents/sensorcam' '/media/burak/New Volume/archive/sensorcam' --delete")
#    os.system("python rsync.py '/home/burak/Documents/bass' '/media/burak/New Volume/archive/bass' --delete")

if sys.argv[1] == "kitaplar":
    os.system("python rsync.py  '/media/burak/New Volume/archive/kitaplar' /media/burak/A75E-5A13/kitaplar --delete")

if sys.argv[1] == "flash":
#    os.system("python rsync.py '/home/burak/Documents/bass' '/media/burak/7502-42E6/bass' --delete")
    os.system("python rsync.py '/home/burak/Documents/kod' '/media/burak/7502-42E6/kod' --delete")
    os.system("python rsync.py '/home/burak/Documents/classnotes' '/media/burak/7502-42E6/classnotes' --delete")
#    os.system("python rsync.py '/home/burak/Documents/sensorcam' '/media/burak/7502-42E6/sensorcam' --delete")
    os.system("python rsync.py '/home/burak/Documents/books' '/media/burak/7502-42E6/books' --delete")
    
if sys.argv[1] == "hd2hd":
 #   os.system("python rsync.py '/media/burak/New Volume/archive/bass' '/media/burak/17EA-3758/archive/bass' --delete")
    os.system("python rsync.py '/media/burak/New Volume/archive/Dropbox' '/media/burak/17EA-3758/archive/Dropbox' --delete")
    os.system("python rsync.py '/media/burak/New Volume/archive/kitaplar' '/media/burak/17EA-3758/archive/kitaplar' --delete")
    os.system("python rsync.py '/media/burak/New Volume/archive/kod' '/media/burak/17EA-3758/archive/kod' --delete")
    os.system("python rsync.py '/media/burak/New Volume/archive/classnotes' '/media/burak/17EA-3758/archive/classnotes' --delete")
    os.system("python rsync.py '/media/burak/New Volume/archive/books' '/media/burak/17EA-3758/archive/books' --delete")
    os.system("python rsync.py '/media/burak/New Volume/archive/data' '/media/burak/17EA-3758/archive/data' --delete")


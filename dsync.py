import os, sys, shutil, rsync, re

if len(sys.argv) < 2: print "Usage dsync.py [letter]"; exit()

if sys.argv[1] == "hd":
    os.system("python rsync.py '/home/burak/Documents/bass' '/media/burak/New Volume/archive/bass' --delete")
    os.system("python rsync.py '/home/burak/Documents/Dropbox' '/media/burak/New Volume/archive/Dropbox' --delete")
    os.system("python rsync.py '/home/burak/Documents/kod' '/media/burak/New Volume/archive/kod' --delete")
    os.system("python rsync.py '/home/burak/Documents/classnotes' '/media/burak/New Volume/archive/classnotes' --delete")
    os.system("python rsync.py '/home/burak/Documents/books' '/media/burak/New Volume/archive/books' --delete")

if sys.argv[1] == "kitaplar":
    os.system("python rsync.py /media/burak/6A4D-5BF0/kitaplar '/media/burak/New Volume/archive/kitaplar' --delete")

if sys.argv[1] == "flash":
    os.system("python rsync.py '/home/burak/Documents/bass' '/media/burak/UUI/backup/bass' --delete")
    os.system("python rsync.py '/home/burak/Documents/kod' '/media/burak/UUI/backup/kod' --delete")
    os.system("python rsync.py '/home/burak/Documents/classnotes' '/media/burak/UUI/backup/classnotes' --delete")
    

import os, sys, shutil, rsync, re

if len(sys.argv) < 2: print "Usage dsync.py [letter]"; exit()

if sys.argv[1] == "hd":
    os.system("python rsync.py c:\\Users\\burak\\Documents\\Dropbox e:\\archive\\Dropbox --delete")
    os.system("python rsync.py c:\\Users\\burak\\Documents\\classnotes e:\\archive\\classnotes --delete")
    os.system("python rsync.py c:\\Users\\burak\\Documents\\kod e:\\archive\\kod --delete")
    os.system("python rsync.py c:\\Users\\burak\\Documents\\bass e:\\archive\\bass --delete")

if sys.argv[1] == "hd2":
    os.system("python rsync.py c:\\Users\\burak\\Documents\\Dropbox d:\\archive\\Dropbox --delete")
    os.system("python rsync.py c:\\Users\\burak\\Documents\\classnotes d:\\archive\\classnotes --delete")
    os.system("python rsync.py c:\\Users\\burak\\Documents\\kod d:\\archive\\kod --delete")
    os.system("python rsync.py c:\\Users\\burak\\Documents\\bass d:\\archive\\bass --delete")

if sys.argv[1] == "kitaplar":
    os.system("python rsync.py d:\\kitaplar e:\\archive\\kitaplar --delete")

if sys.argv[1] == "test":
    os.system("python rsync.py '/home/burak/Documents/test' '/home/burak/Downloads/New Volume/test' --delete")

if sys.argv[1] == "flash":
    os.system("python rsync.py '/home/burak/Documents/bass' '/media/burak/UUI/backup/bass' --delete")
    os.system("python rsync.py '/home/burak/Documents/Dropbox' '/media/burak/UUI/backup/Dropbox' --delete")
    os.system("python rsync.py '/home/burak/Documents/kod' '/media/burak/UUI/backup/kod' --delete")
    os.system("python rsync.py '/home/burak/Documents/classnotes' '/media/burak/UUI/backup/classnotes' --delete")
    

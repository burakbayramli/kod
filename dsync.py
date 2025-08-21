import os, sys, rsync, glob

if len(sys.argv) < 2:
    print ("Usage dsync.py [letter]")
    exit()

if sys.argv[1] == "acer_ext":
    tmp = glob.glob('/home/burak/Documents/kitaplar/*')
    if (len(tmp)==0):
        print ('\n============= PARTITION NOT MOUNTED =================')
        exit()
    os.system("python rsync.py '/home/burak/Documents/Dropbox' '/media/burak/Backup Plus/archive/Dropbox' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/tw' '/media/burak/Backup Plus/archive/repos/tw'  --ignore-list=.git --delete 1")
    os.system("python rsync.py '/home/burak/Documents/classnotes' '/media/burak/Backup Plus/archive/classnotes'  --ignore-list=.git  --delete 1")
    os.system("python rsync.py '/home/burak/Documents/kitaplar' '/media/burak/Backup Plus/archive/kitaplar'  --delete 1")
    os.system("python rsync.py '/opt/Downloads/dotbkps' '/media/burak/Backup Plus/archive/dotbkps'  --delete 1")
    
if sys.argv[1] == "ext1_ext2":
    os.system("python rsync.py '/media/burak/Backup Plus/shows' '/media/burak/Seagate Backup Plus Drive/shows'   --delete 1")
    os.system("python rsync.py '/media/burak/Backup Plus/archive/Dropbox' '/media/burak/Seagate Backup Plus Drive/archive/Dropbox'  --delete 1")
    os.system("python rsync.py '/media/burak/Backup Plus/archive/data' '/media/burak/Seagate Backup Plus Drive/archive/data'   --delete 1")
    os.system("python rsync.py '/media/burak/Backup Plus/archive/dotbkps' '/media/burak/Seagate Backup Plus Drive/archive/dotbkps'   --delete 1")
    os.system("python rsync.py '/media/burak/Backup Plus/archive/kitaplar' '/media/burak/Seagate Backup Plus Drive/archive/kitaplar'  --delete 1")
    os.system("python rsync.py '/media/burak/Backup Plus/Lectures' '/media/burak/Seagate Backup Plus Drive/Lectures'  --delete 1")
    os.system("python rsync.py '/media/burak/Backup Plus/archive/dotbkps' '/media/burak/Seagate Backup Plus Drive/archive/dotbkps'  --delete 1")

if sys.argv[1] == "usb64_nano":
    os.system("python rsync.py '/media/1BC3-0618/archive/kitaplar' '/home/burak/Documents/kitaplar'  --delete 1")
                    
if sys.argv[1] == "usb64_pi":
    os.system("python rsync.py '/media/pi/1BC3-0618/archive/kitaplar' '/home/pi/Documents/kitaplar'  --delete 1")
                    
if sys.argv[1] == "acer_nano":
    os.system("rsync -aP ~/Documents/kitaplar/* burak@192.168.43.34:/home/burak/Documents/kitaplar")
                    
if sys.argv[1] == "acer_a9_pull": # run it on a9
    os.system("rsync -aP burak@192.168.43.34:/home/burak/Documents/kitaplar/* /home/burak/Documents/kitaplar ")
                    
if sys.argv[1] == "acer_usb64":
    tmp = glob.glob('/home/burak/Documents/kitaplar/*')
    os.system("python rsync.py '/home/burak/Documents/Dropbox' '/media/burak/1BC3-0618/archive/Dropbox' --delete 1")
    os.system("python rsync.py '/home/burak/Documents/tw' '/media/burak/1BC3-0618/archive/repos/tw'  --ignore-list=.git  --delete 1")
    os.system("python rsync.py '/home/burak/Documents/classnotes' '/media/burak/1BC3-0618/archive/classnotes'  --ignore-list=.git  --delete 1")
    os.system("python rsync.py '/home/burak/Documents/kitaplar' '/media/burak/1BC3-0618/archive/kitaplar'  --delete 1")
    os.system("python rsync.py '/opt/Downloads/dotbkps' '/media/burak/1BC3-0618/archive/dotbkps'  --delete 1")
                    
if sys.argv[1] == "acer_usbred":
    os.system("python rsync.py '/home/burak/Documents/Dropbox' '/media/burak/RED1/archive/Dropbox' --delete 1")
    os.system("python rsync.py '/opt/Downloads/dotbkps' '/media/burak/RED1/archive/dotbkps'  --delete 1")
    

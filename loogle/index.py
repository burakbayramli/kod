import sys; sys.path.append('..')
import loogle3, rsync

# notebook, usb
index_db = "/home/burak/Downloads/campdata/loogle3.db"
cdir = "/home/burak/Documents/kitaplar"
loogle3.index(cdir, index_db)

# notebook, seagate hd
#index_db = "/media/burak/Seagate Backup Plus Drive/archive/data/loogle3.db"
#cdir = "/media/burak/Seagate Backup Plus Drive/archive/kitaplar"
#loogle3.index(cdir, index_db)

# usb60
#index_db = "/data/data/com.termux/files/home/Downloads/Dropbox/loogle3.db"
#cdir = "/storage/1BC3-0618/archive/kitaplar"
#loogle3.index(cdir, index_db)

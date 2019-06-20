import sys; sys.path.append('..')
import loogle3, rsync

# notebook
index_db = "/media/burak/1BC3-0618/archive/data/loogle3.db"
cdir = "/media/burak/1BC3-0618/archive/kitaplar"
loogle3.index(cdir, index_db)

# usb60
#index_db = "/data/data/com.termux/files/home/Downloads/Dropbox/loogle3.db"
#cdir = "/storage/1BC3-0618/archive/kitaplar"
#loogle3.index(cdir, index_db)

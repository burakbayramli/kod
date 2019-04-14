import sys; sys.path.append('..')
import loogle3, rsync

index_db = "/home/burak/Downloads/loogle.db"
cdir = "/media/burak/1BC3-0618/archive/kitaplar"

loogle3.index(cdir, index_db)
#loogle3.index(cdir, index_db, new_index=True)

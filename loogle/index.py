import sys; sys.path.append('..')
import loogle3, rsync

index_dir = "/home/burak/Downloads/loogle.db"
cdir = "/media/burak/23B9-71E6/kitaplar"

loogle3.index(cdir, index_dir, new_index=False)
#loogle3.index(cdir, index_dir, new_index=True)

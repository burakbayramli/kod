import sys; sys.path.append('..')
import loogle2, rsync

index_dir = "/tmp/.loogle"
cdir = "/media/burak/23B9-71E6/kitaplar"

loogle2.index(cdir, index_dir, new_index=True, stop_after_n=40)
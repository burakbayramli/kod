import os, sys

fr_file = sys.argv[1]
to_file = fr_file.replace(".pdf",".jpg")
to_dir = sys.argv[2]

cmd = 'convert -density 200x200 -quality 60 %s %s/%s'
os.system(cmd % (fr_file,to_dir,to_file))

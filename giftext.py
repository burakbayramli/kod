# ffmpeg -y -ss 00:01:15 -i in.mp4 -t 00:00:06 -c copy out.mp4
import os

w = '"fps=10,scale=360:-1:flags=lanczos,'
w += "drawtext=enable='between(t,0,1)':fontfile=font3.ttf:text='T1':fontsize=15:fontcolor=white:x=30:y=170,"
w += "drawtext=enable='between(t,1,3)':fontfile=font3.ttf:text='T2':fontsize=15:fontcolor=white:x=30:y=170,"
w += "drawtext=enable='between(t,3,6)':fontfile=font3.ttf:text='T3':fontsize=15:fontcolor=white:x=30:y=170,"
w += "drawtext=enable='between(t,6,11)':fontfile=font3.ttf:text='T4':fontsize=15:fontcolor=white:x=30:y=170,"
w += 'split[x1][x2];[x1]palettegen[p];[x2][p]paletteuse"'

cmd = "/usr/bin/ffmpeg -i vid.mp4 -filter_complex " + w + " output.gif"

print (cmd)

os.system(cmd)

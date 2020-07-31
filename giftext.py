import subprocess, os

w = '"fps=10,scale=360:-1:flags=lanczos,'
w += "drawtext=enable='between(t,0,2)':fontfile=font3.ttf:text='Txt1':fontsize=15:fontcolor=white:x=(w-tw)/2:y=(h/PHI)+th,"
w += "drawtext=enable='between(t,2,5)':fontfile=font3.ttf:text='Txt2':fontsize=15:fontcolor=white:x=(w-tw)/2:y=(h/PHI)+th,"
w += "drawtext=enable='between(t,5,7)':fontfile=font3.ttf:text='Txt3':fontsize=15:fontcolor=white:x=(w-tw)/2:y=(h/PHI)+th,"
w += "drawtext=enable='between(t,7,9)':fontfile=font3.ttf:text='Txt4':fontsize=15:fontcolor=white:x=(w-tw)/2:y=(h/PHI)+th,"
w += 'split[x1][x2];[x1]palettegen[p];[x2][p]paletteuse"'

cmd = "/usr/bin/ffmpeg -ss 47 -t 9 -i video.mp4 -filter_complex " + w + " output.gif"

print (cmd)

os.system(cmd)

# ffmpeg -y -ss 00:01:15 -i in.mp4 -t 00:00:06 -c copy out.mp4
# ffmpeg -y -i out.mp4 -filter_complex "fps=10,scale=360:-1:flags=lanczos,split[x1][x2];[x1]palettegen[p];[x2][p]paletteuse" output.gif
import os

def avigif(pieces, text):
    files = ""
    for i,(file,start,dur) in enumerate(pieces):
        cmd = 'ffmpeg -y -ss %s -i %s -t %s -c copy /tmp/out-%d.mp4' % (start, file, dur, i)
        print (cmd)
        os.system(cmd)       
        cmd = 'ffmpeg -y -i /tmp/out-%d.mp4 -filter_complex "fps=10,scale=360:-1:flags=lanczos,split[x1][x2];[x1]palettegen[p];[x2][p]paletteuse" /tmp/out-%d.gif' % (i,i)
        files += "/tmp/out-%d.gif " % i 
        print (cmd)
        os.system(cmd)
    
    cmd = "convert %s /tmp/output1.gif" % files
    print (cmd)
    os.system(cmd)
        
    w = '"fps=10,scale=360:-1:flags=lanczos,'
    for i,(start,finish,text,pos) in enumerate(text):  
        w += "drawtext=enable='between(t,%s,%s)':fontfile=font3.ttf:text='%s':fontsize=15:fontcolor=white:x=%s:y=%s," % (start,finish,text,pos[0],pos[1])
    w += 'split[x1][x2];[x1]palettegen[p];[x2][p]paletteuse"'
    cmd = "/usr/bin/ffmpeg -y -i /tmp/output1.gif -filter_complex " + w + " /tmp/output2.gif"
    print (cmd)
    os.system(cmd)
        

if __name__ == "__main__": 
 
    ps = [['devil.mp4','00:01:06','00:00:9']]
    text = [[0,2.5,'Big guns and big whips',(80,170)],
            [3,5,'Rich talkin big shit',(80,170)],
            [5.5,7,'Double cup, gold wrist',(80,170)],
            [7.5,9,'Double up on that blow, bitch!',(80,170)]]
    
    avigif(ps,text)
    

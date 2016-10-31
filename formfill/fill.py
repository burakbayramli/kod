import os, sys
import PIL
from PIL import ImageFont
from PIL import Image
from PIL import ImageDraw
import re

out = sys.argv[1] + "-out.jpg"
loc = sys.argv[1] + ".loc"
fill = sys.argv[1] + ".fill"

img = Image.open(sys.argv[1])

try:
    fillfile = open(fill,"r")
    locfile = open(loc,"r")
except Exception, e: 
    img.save(out)    
    exit()
    
for line in fillfile:
    font_size = 20
    locline = locfile.readline()
    line=line.replace("\n","")
    locline=locline.replace("\n","")
    x = float(locline.split(" ")[0]) 
    y = float(locline.split(" ")[1]) 
    print x,y,line    

    res = re.search("down=(\d*)",line)
    if res: 
        y+=int(res.group(1))
        line=re.sub("\[down=\d*\]","",line)        
        
    res = re.search("up=(\d*)",line)
    if res: 
        y-=int(res.group(1))
        line=re.sub(r'\[up=\d*\]','',line)
        
    res = re.search("left=(\d*)",line)
    if res: 
        x-=int(res.group(1))
        line=re.sub(r'\[left=\d*\]','',line)
        
    res = re.search("right=(\d*)",line)
    if res: 
        x+=int(res.group(1))
        line=re.sub(r'\[right=\d*\]','',line)
        
    res = re.search("font=(\d*)",line)
    if res: 
        font_size=int(res.group(1))
        line=re.sub(r'\[font=\d*\]','',line)

    res = re.search("sign=(0.\d*)",line)
    if res: 
        scale=float(res.group(1))
        line=re.sub("\[sign=0.\d*\]","",line)        
        sign = Image.open("signature.jpg")        
        print sign.size
        sizex  = sign.size[0]
        sizey  = sign.size[1]
        new_sizex  = int(sign.size[0]*scale)
        new_sizey  = int(sign.size[1]*scale)
        sign = sign.resize((new_sizex, new_sizey))
        img.paste(sign, (int(x),int(y),int(x)+new_sizex,int(y)+new_sizey))
        draw = ImageDraw.Draw(img)
                       
    for line in line.split("\\"):                    
        draw = ImageDraw.Draw(img)    
        font = ImageFont.truetype("/usr/share/fonts/truetype/ubuntu-font-family/Ubuntu-C.ttf", font_size)
        draw.text((x, y), line, font=font,fill='black')
        y += 30

img.save(out)

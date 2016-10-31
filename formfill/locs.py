import sys

import Tkinter
from PIL import ImageDraw, Image, ImageTk
import sys

window = Tkinter.Tk(className="bla")

image = Image.open(sys.argv[1])

x = 1000.
y = 700.
xratio = image.size[0]/x
yratio = image.size[1]/y
print "ratio", xratio, yratio

out = open(sys.argv[1]+".loc", "w")
image = image.resize((int(x), int(y)), Image.ANTIALIAS)
canvas = Tkinter.Canvas(window, width=image.size[0], height=image.size[1])
canvas.pack()
image_tk = ImageTk.PhotoImage(image)

canvas.create_image(image.size[0]//2, image.size[1]//2, image=image_tk)

def callback(event):
    print "clicked at: ", event.x, event.y
    xx = float(event.x) * xratio
    yy = float(event.y) * yratio    
    out.write(str(xx) + " " + str(yy))
    out.write("\n")
    out.flush()

canvas.bind("<Button-1>", callback)
Tkinter.mainloop()

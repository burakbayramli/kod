# Start with imgclick.py [image file] and on the screen
# click on location to collect its pixel coordinates.
from PIL import ImageDraw, Image, ImageTk
import sys, tkinter

window = tkinter.Tk(className="bla")
image = Image.open(sys.argv[1])
print ('size',image.size[0],image.size[1])

image = image.resize((int(1400), int(900)), Image.ANTIALIAS)
canvas = tkinter.Canvas(window, width=image.size[0], height=image.size[1])
canvas.pack()
image_tk = ImageTk.PhotoImage(image)

canvas.create_image(image.size[0]//2, image.size[1]//2, image=image_tk)

def callback(event):
    print ("[%d,%d]," % (event.x, event.y))

canvas.bind("<Button-1>", callback)
tkinter.mainloop()

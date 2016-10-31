import gtk.gdk
import PIL.Image

w = gtk.gdk.get_default_root_window()
sz = w.get_size()
print "The size of the window is %d x %d" % sz

start_x = 0
start_y = 50
size_x = 650
size_y = 480
end_x = start_x+size_x
end_y = start_y+size_y
box = (start_x, start_y, start_x+size_x, start_y+size_y)

w = gtk.gdk.get_default_root_window()
sz = w.get_size()

fwd = False

for i in range(20):
    pb = gtk.gdk.Pixbuf(gtk.gdk.COLORSPACE_RGB,False,8,sz[0],sz[1])
    pb = pb.get_from_drawable(w,w.get_colormap(),0,0,0,0,sz[0],sz[1])
    width,height = pb.get_width(),pb.get_height()
    im = PIL.Image.fromstring("RGB",(width,height),pb.get_pixels())
    im = im.crop(box)
    f = "/tmp/out-%02d.png" % i
    im.save(f)
    

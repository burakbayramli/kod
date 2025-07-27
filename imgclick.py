'''
Start with imgclick.py [image file] and on the screen
click on location to collect its pixel coordinates.
'''
def click_geo(refc, refp, clicks):
    if len(refc) != 2 or len(refp) != 2:
        raise ValueError("`refc` and `refp` must each contain exactly two reference points.")
    if any(len(p) != 2 for p in refc) or any(len(p) != 2 for p in refp):
        raise ValueError("All reference points must be 2-element lists [x, y] or [lat, lon].")
    if not isinstance(clicks, list) or not all(isinstance(c, list) and len(c) == 2 for c in clicks):
        raise ValueError("`clicks` must be a list of 2-element lists [x, y].")

    x1_pix, y1_pix = refc[0]
    x2_pix, y2_pix = refc[1]

    lat1_geo, lon1_geo = refp[0]
    lat2_geo, lon2_geo = refp[1]

    delta_x_pix = x2_pix - x1_pix
    delta_y_pix = y2_pix - y1_pix

    delta_lat_geo = lat2_geo - lat1_geo
    delta_lon_geo = lon2_geo - lon1_geo

    lat_scale_per_pixel = delta_lat_geo / delta_y_pix
    lat_offset = lat1_geo - (lat_scale_per_pixel * y1_pix)

    lon_scale_per_pixel = delta_lon_geo / delta_x_pix
    lon_offset = lon1_geo - (lon_scale_per_pixel * x1_pix)

    converted_coords = []
    for click_x, click_y in clicks:
        new_lat = (lat_scale_per_pixel * click_y) + lat_offset
        new_lon = (lon_scale_per_pixel * click_x) + lon_offset
        converted_coords.append([new_lat, new_lon])

    return converted_coords

if __name__ == "__main__":

    from PIL import ImageDraw, Image, ImageTk
    import sys, tkinter    
 
    window = tkinter.Tk(className="bla")
    image = Image.open(sys.argv[1])
    print ('size',image.size[0],image.size[1])

    image = image.resize((int(1400), int(900)), Image.LANCZOS)
    canvas = tkinter.Canvas(window, width=image.size[0], height=image.size[1])
    canvas.pack()
    image_tk = ImageTk.PhotoImage(image)

    canvas.create_image(image.size[0]//2, image.size[1]//2, image=image_tk)

    def callback(event):
        print ("[%d,%d]," % (event.x, event.y))

    canvas.bind("<Button-1>", callback)
    tkinter.mainloop()

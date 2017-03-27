#!/usr/bin/env python

# Takes a picture. Press 't' to save the screenshot in my-pic-1.jpg,
# my-pic-2.jpg, etc.. Every press will increment the number.
import cv2, time

class Capture:
    
    def save(self, pic):
        cv2.SaveImage('my-pic-' + str(self.i) + '.jpg', pic)
    
    def __init__(self):
        self.i = 1
        self.capture = cv2.CaptureFromCAM(0)
        cv2.NamedWindow( "CamShiftDemo", 1 )

        print( "Keys:\n"
            "    ESC - quit the program\n"
            "    t - take picture\n"
            "    o - timed picture\n"
            "To initialize tracking, drag across the object with the mouse\n" )


    def run(self):
        while True:
            frame = cv2.QueryFrame( self.capture )
            cv2.ShowImage( "CamShiftDemo", frame )
            c = cv2.WaitKey(7)
            if c == 27:
                break
            elif c == ord("t"):
                frame = cv2.QueryFrame( self.capture )
                self.save(frame)
                self.i += 1
            elif c == ord("o"):
                # timed pictures, press and it starts taking pictures
                # every 3 seconds
                j = 0
                while True:
                    j += 1
                    frame = cv2.QueryFrame( self.capture )
                    cv2.ShowImage( "CamShiftDemo", frame )
                    c = cv2.WaitKey(7)
                    if j % 16 == 0: 
                        self.save(frame)
                        self.i += 1
                        time.sleep(2)
                

if __name__=="__main__":
    demo = Capture()
    demo.run()

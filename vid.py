#!/usr/bin/env python

#
# Simple recorder that uses OpenCV. Records webcam into a file
# called movie.avi.

import cv
import cv2

class Capture:
    
    def __init__(self):
        self.capture = cv.CaptureFromCAM(0)
        cv.NamedWindow( "CamShiftDemo", 1 )

        print( "Keys:\n"
            "    ESC - quit the program\n"
            "    t - take picture\n"
            "To initialize tracking, drag across the object with the mouse\n" )

    def run(self):
        fps =  30
        frame = cv.QueryFrame( self.capture )
        frame_size = cv.GetSize (frame)
        writer=cv.CreateVideoWriter('movie.avi',cv2.cv.CV_FOURCC('F', 'M', 'P', '4'),fps,frame_size)
        while True:
            frame = cv.QueryFrame( self.capture )
            cv.ShowImage( "CamShiftDemo", frame )
            c = cv.WaitKey(7)
            cv.WriteFrame(writer,frame)
            if c == 27:
                break
            
        cv.ReleaseVideoWriter (writer)
  
if __name__=="__main__":
    demo = Capture()
    demo.run()

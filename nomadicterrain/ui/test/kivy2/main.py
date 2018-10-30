import kivy
kivy.require('1.0.6')

from glob import glob
from random import randint
from os.path import join, dirname
from kivy.app import App
from kivy.logger import Logger
from kivy.uix.scatter import Scatter
from kivy.uix.image import Image
from kivy.properties import StringProperty


class Picture(Image):
    '''Picture is the class that will show the image with a white border and a
    shadow. They are nothing here because almost everything is inside the
    picture.kv. Check the rule named <Picture> inside the file, and you'll see
    how the Picture() is really constructed and used.

    The source property will be the filename to show.
    '''

    source = StringProperty(None)


class PicturesApp(App):

    def build(self):

        print ('inside build-------------------------------')
        # the root is created in pictures.kv
        root = self.root

        # get any files into images directory
        #filename = '/home/burak/Downloads/milk.jpg'
        filename = '/sdcard/Download/out.png'
        picture = Picture(source=filename)
        # add to the main field
        root.add_widget(picture)

    def on_pause(self):
        return True


if __name__ == '__main__':
    PicturesApp().run()

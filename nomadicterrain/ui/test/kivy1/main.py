from kivy.app import App
from kivy.uix.button import Button

def callback(instance):
    print('The button <%s> is being pressed' % instance.text)
    fout = open("/sdcard/Download/kivy-out.txt","aw")
    fout.write("filan falan fisman\n")
    fout.close()


class TestApp(App):
    def build(self):
        btn1 = Button(text='Hello World')
        btn1 = Button(text='Hello world 1')
        btn1.bind(on_press=callback)
        return btn1
        
TestApp().run()

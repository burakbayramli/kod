export ANDROIDSDK="/home/burak/Downloads"
export ANDROIDNDK="/home/burak/Downloads"
export ANDROIDAPI="21"  # Target API version of your application
export ANDROIDNDKVER="r18b"  # Version of the NDK you installed
p4a apk --private /home/burak/Documents/kod/nomadicterrain/ui/test/kivy1 --package=org.example.myapp --name "My application" --version 0.1 --bootstrap=sdl2 --requirements=python3,kivy

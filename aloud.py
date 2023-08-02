import gtts, os, sys
cmd = "pdftotext -f %d -l %d %s /tmp/out.txt"
cmd = cmd % (int(sys.argv[2]), int(sys.argv[3]), sys.argv[1])
os.system(cmd)
content = open("/tmp/out.txt").read()
tts = gtts.gTTS(text=content, lang="en")
filename = "/tmp/out.mp3"
tts.save(filename)

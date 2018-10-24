import textract, pyttsx3
from gtts import gTTS
import os

text = textract.process("shipof.epub")
t = str(text[10000:20000] )
tts = gTTS(text=t, lang='en')
tts.save("good.mp3")


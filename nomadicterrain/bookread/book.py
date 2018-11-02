import textract, json, codecs
from gtts import gTTS

def book_extract(file_in, from_perc, to_perc):
    text = textract.process(file_in,encoding='ascii')
    L = len(text)
    from_l = int((L * from_perc) / 100.0)
    to_l = int((L * to_perc) / 100.0)
    t = str(text[from_l:to_l] )
    fout = codecs.open ("/tmp/out.txt","w","utf-8")
    fout.write(t)
    fout.close()

def text_to_sound(fin):
    content = open(fin).read()
    tts = gTTS(text=content, lang='en')
    tts.save("/tmp/out.mp3")
    
book_extract("/home/burak/Downloads/scaramuc.epub", 10, 11)
text_to_sound("/tmp/out.txt")

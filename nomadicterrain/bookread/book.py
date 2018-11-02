import textract, json, codecs, os
from gtts import gTTS

def book_extract(file_in, from_perc, to_perc, file_out):
    text = textract.process(file_in,encoding='ascii')
    L = len(text)
    from_l = int((L * from_perc) / 100.0)
    to_l = int((L * to_perc) / 100.0)
    t = str(text[from_l:to_l] )
    fout = codecs.open (file_out,"w","utf-8")
    fout.write(t)
    fout.close()

def text_to_sound(fin, fout):
    content = open(fin).read()

if __name__ == "__main__": 
     
    book_extract(os.environ['HOME'] + "/Downloads/scaramuc.epub", 10, 11, "out.txt")
    text_to_sound("out.txt", "out.mp3")

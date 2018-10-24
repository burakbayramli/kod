import textract, json, codecs

params = json.loads(open("in/params.json").read())

text = textract.process(params['file_in'],encoding='ascii')
L = len(text)
from_l = int((L * float(params['from_perc'])) / 100.0)
to_l = int((L * float(params['to_perc'])) / 100.0)
t = str(text[from_l:to_l] )
fout = codecs.open ("out/out.txt","w","utf-8")
fout.write(t)
fout.close()

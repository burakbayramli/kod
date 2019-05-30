# SendGrid, SMTP, Email Servisi

GMail'in SMTP servisi var fakat normal email göndermek için
kullandığımız servisi bir ticari uygulamadan mail göndermek için
kullandığımızda bazı problemler çıkabiliyor. Google'ın güvenlik
kontrolleri kişileri korumak üzere yapılmış, bu sebeple kışisel mail
servisinizi uygulamadan kullanmak istediğimizde her adımda zorluk
çıkıyor.

Daha iyisi işi sadece uygulamalara hizmet etmek için yazılmış bir
servis kullanmak. SendGrid böyle bir uygulama.

sendgrid.com

Bedava servise kaydoluyoruz, sonra api anahtarı üretiyoruz, bu
üretilen anahtar şifre, kullanıcı ismi ise `apıkey` oluyor, bu şekilde
SMTP servisine kavuşuyoruz. Örnek kod

```
import smtplib
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText

mail_from = 'Burak Bayramli <....@gmail.com>'
mail_to = 'Burak Bayramli <...@gmail.com>'

msg = MIMEMultipart()
msg['From'] = mail_from
msg['To'] = mail_to
msg['Subject'] = 'Sending mails with Python'
mail_body = """
Hey,

This is a test.

Sevgiler,\nB

"""
msg.attach(MIMEText(mail_body))

try:
    server = smtplib.SMTP_SSL('smtp.sendgrid.net', 465)
    server.ehlo()
    server.login('apikey', '[anahtar]')
    server.sendmail(mail_from, mail_to, msg.as_string())
    server.close()
    print("mail sent")
except:
    print("issue")
```

Sendgrid'in bir entegrasyon kontrol mekanizması var; doğrulama yapmak
için kendinize bir email atmanızı bekliyor, attıktan sonra (üstteki
kodla yapılabilir), "Verify"'a tıklıyoruz, Sendgrid sisteminde bu
mail'i arıyor, varsa entegrasyon tamamlandı diyor.

Bedava servisin günlük kotası yüzlerce email, başlangıç için yeterli
olur herhalde.


Kaynaklar

[1] https://blog.ruanbekker.com/blog/2018/08/21/send-emails-using-python-and-sendgrid-using-smtplib/









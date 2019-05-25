# Python ile Mail Göndermek, SMTP, GMail

Dışarıdan Python ile GMail'e bağlanıp email göndermek istersek, önce
email göndereceğimiz hesaba girip, Account ayarlarına gideriz,
Security altında "Less secure apps"'lere erişim için izin
veririz. Burada Google bir sürü uyarı verecek, bunları geçip kullanıma
geçebiliriz. 

Şifreyi kod içine koymak iyi değil tabii, onu `$HOME` altında `.şifre`
adlı bir dosya içine koyabiliriz. Basit bir şifrelemeden sonra koymak
ta iyi olabilir.

Artık kullanıcı / şifre ile GMail'e email attırabiliriz.


```
import smtplib, os

gmail_user = 'kaynakkullanici@gmail.com'  
gmail_password = open(os.environ['HOME'] + "/.sifre").read()

sent_from = gmail_user 
to = ['hedefkullanici@gmail.com']  
subject = 'OMG Super Important Message'  
body = 'Hey, whats up?\n\n- You'

email_text = """
From: %s  
To: %s  
Subject: %s

%s
""" % (sent_from, ", ".join(to), subject, body)

try:  
    server = smtplib.SMTP_SSL('smtp.gmail.com', 465)
    server.ehlo()
    server.login(gmail_user, gmail_password)
    server.sendmail(sent_from, to, email_text)
    server.close()

    print ('Email sent!')
except Exception as e:
    print (repr(e))
    print ('Something went wrong...')

```


[1] https://stackabuse.com/how-to-send-emails-with-gmail-using-python/


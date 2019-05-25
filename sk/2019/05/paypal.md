# Paypal Ödemeleri

Paypal Türkiye'den kullanılmıyor fakat ileride açılabilir, ayrıca
yabancı ülkelerde banka hesapları üzerinden kullanılabilir. Yazının
geri kalanı işler bir Paypal hesabı olduğunu farz ediyor.

Eğer normal paypal.com hesabınız varsa, onunla alakalı

https://developer.paypal.com

hesabınız da olacaktır. Bu adresten Paypal'ın geliştirme ve test
araçlarına erisebiliyorsunuz. Üstteki adrese paypal kullanıcı /
şifremiz ile giriş yapalım. Burada bazı taklit satıcı, müşteri
hesapları yaratacağız. Accounts'a tıklayın orada kum kabı (sandbox)
hesapları yaratabiliriz. "Kum kabı" içinde "kumda kale yapar gibi" bu
taklit hesaplar ile işlem gerçek işlemlere yakın hareketler
yapacağız. Bir tane iş (business) hesabı yaratalim ki bu hesaptan
ödeme kabul edebilelim, diğeri kişisel taklit hesap olabilir, ki ödeme
gonderebilelim. Create Account ile bu hesapları yaratırız. Hesap
yaratırken kullanılan email ve şifre ile şimdi

sandbox.paypal.com

adresine giriş yapabiliriz. Bu adres bizi bu taklit kullanıcıların
"hesabına" götürüyor. Bu kullanıcılara neredeyse tam tekmilli paypal
hesaplariymis gibi, o sayfalar üzerinden erisebilmek önemli böylece bu
hesaplarda oynamalar, değişiklikler yaparak farklı test senaryolarını
deneyebiliriz, ama bu testler gerçek hesabımızı etkilemiyor.

Bir "satın al" butonu yaratalım. Satıcının hesabına girelim, menüden
Tools | Business setup seçelim. Payment setup'tan "on your website"
seçip Continue tıklayalım. Sonraki sayfada ödemeleri nasıl işlemek
istediğimiz soruluyor, sol blok seçilebilir, oradaki Continue
seçilebilir böylece kredi kartı işlemi için düğme / buton yaratıyoruz.

Step 1 diyen, bir sonraki ekranda "create payment buttons using HTML".
Düğme tipi için "Buy Now" tipi yeterli, en basiti bu.

Step 2 altında "save button at Paypal" işaretini iptal edelim. Bir
ürün fiyatı girelim, mesela 10 dolar.

Step 3 altında Add advances variables kısmı var, bu kutu önemli,
Paypal ödemeyi aldıktan sonra bizim sitemize bildirimi nasıl yapacak,
onu gösteriyor. Bu kutuya

```
notify_url=[bizim site ve paypal url]
```

Simdi create button ile düğmeyi üretelim. Düğme bir HTML kodu olarak
bize verilecek, bu kodu sitemizin HTML'i içine koyunca bir ödeme
düğmesi göreceğiz. Bu düğmeyi bir HTML'e koyalım, sayfayı tarayıcıda
ziyaret edelim ve satın alma düğmesini görelim. Ona tıklayaylım,
paypal'e gideceğiz, burada taklit alıcı hesabıyla giriş yapıp ödemeyi
yapalım. Ödeme şekillerinde kredi kart, paypal hesabı gibi seçenekler
var, taklit kredi kartı da kullanabilirdik. Son birkaç rakamı
gösterilen kart gerçek değil. Ödeme düğmesi bizi sandbox alanına
götürmüştü, çünkü düğmeyi bu taklit alanda yaratmıştık.

Artık www.sandbox.paypal.com adresine gidersek burada hesabımızdan 10
dolar eksiltildiğini görürüz. Bu tabii ki taklit para. Şimdi satıcı
hesabına girersek orada 10 dolara yakın bir paranın hesabımıza
geçtiğini görüyoruz (10 dolardan biraz eksik olabilir çünkü paypal
komisyonunu kesti).

Paypal'den Bilgi Almak

Üstte `notify_url` ile bir URL verdik. Bu URL bizim sitemizde mesela
Flask ile servis ettiğimiz / dinlendiğimiz bir adres olacak. Flask'te
şu kodlar kullanılabilir,

```
from flask import Flask, jsonify, render_template, request
import requests
import urllib.parse
...
VERIFY_URL_PROD = 'https://ipnpb.paypal.com/cgi-bin/webscr'
VERIFY_URL_TEST = 'https://ipnpb.sandbox.paypal.com/cgi-bin/webscr'
VERIFY_URL = VERIFY_URL_TEST
...
@app.route('/paypal_ipn', methods=['POST','GET'])
def paypal_ipn():    
    params = request.form.to_dict()
    params['cmd'] = '_notify-validate'
    print (params)
    headers = {'content-type': 'application/x-www-form-urlencoded',
               'user-agent': 'Python-IPN-Verification-Script'}
    r = requests.post(VERIFY_URL, params=params, headers=headers, verify=True)
    r.raise_for_status()

    if r.text == 'VERIFIED':
        print ("Verified")
    elif r.text == 'INVALID':
        print ("Invalid")
    else:
        print ("some other response")
        print (r.text)       
    return ""
```

Şimdi Paypal'de ödeme yaptıktan sonra Paypal servis tarafı bizim
servisimiz ile bağlantıya geçecek, `bizim site/paypal_ipn`'e
bağlanacak (bu adresi düğme yaratırken vermiş olmamız lazım tabii), ve
log'da bize gönderdiği bilgileri göreceğiz. Müşteri kodu, email'i,
adresi gibi bir sürü bilgi burada var. Bu bilgileri kaydedip bir
kullanıcı girişi için ileride kullanabiliriz.

Flask Python projemizde gerekli ek paketler `requests`, ve
`urllib3`. Onlar `pip` ile kurulmuş olmalı.


[1] https://www.youtube.com/watch?v=NFUdd3gveN8

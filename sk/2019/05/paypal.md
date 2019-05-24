# Paypal Odemeleri

Paypal Türkiye'den kullanılmıyor fakat ileride açılabilir, ayrıca
yabancı ülkelerde banka hesapları üzerinden kullanılabilir. Yazının
geri kalanı işler bir Paypal hesabı olduğunu farz ediyor.

Eğer normal paypal.com hesabınız varsa, onunla alakalı

https://developer.paypal.com

hesabınız vardır. Bu adresten bazı geliştirme ve test araçlarına
erisebiliyorsunuz. Üstteki adrese paypal kullanıcı / şifreniz ile
giriş yapın. Burada bazı taklit hesaplar yaratacağız. Accounts'a
tıklayın orada kum kabı (sandbox) hesapları yaratmak lazım. Kum kabı
içinde "kumda oynar gibi" bu taklit hesaplar ile işlem gerçek
işlemlere yakın hareketler yapacağız. Bir tane iş (business) hesabı
yaratın ki bu hesaptan ödeme kabul edebilesiniz, diğeri kışisel taklit
hesap olsun, ki ödeme gönderebilesiniz. Create Account ile bu
hesapları yaratırız. Hesap yaratırken kullanılan email ve şifre ile
şimdi

sandbox.paypal.com

adresine giriş yapabilirsiniz. Bu adres bizi bu taklit kullanıcıların
"hesabına" götürüyor. Bu kullanıcılara neredeyse tam tekmilli paypal
sayfaları üzerinden erisebilmek önemli böylece bu hesaplarda
oynamalar, değişiklikler yaparak farklı test senaryolarını
deneyebiliriz, ama bu testler gerçek hesabımızı etkilemiyor.

Bir "satın al" butonu yaratalım. Satıcının hesabına girelim, menüden
Tools | Business setup seçin. Payment setup'tan "on your website"
seçip Continue tıklayalım. Sonraki sayfada ödemeleri nasıl işlemek
istediğiniz soruluyor, sol blok seçilebilir, oradaki Continue
seçilebilir böylece kredi kartı işlemi için düğme / buton
yaratıyoruz. Bir sonraki ekranda "create payment buttons üsing HTML".
Düğme tipi için "Buy Now" tipi yeterli, en basiti bu. Step 2 altında
"save button at Paypal" işaretini iptal edelim. Bir ürün fiyatı
girelim, mesela 10 dolar, şimdi create button ile düğmeyi
üretelim. Düğme bir HTML kodu olarak bize verilecek, bu kodu sitemizin
HTML'i içine koyunca bir ödeme düğmesi göreceğiz. Bu düğmeyi bir
HTML'e koyalım, sayfayı tarayıcıda ziyaret edelim ve satın alma
düğmesini görelim. Ona tıklayaylım, paypal'e gideceğiz, burada taklit
alıcı hesabıyla giriş yapıp ödemeyi yapalım. Ödeme şekillerinde kredi
kart, paypal hesabı gibi seçenekler var, taklit kredi kartı da
kullanabilirdik. Son birkaç rakamı gösterilen kart gerçek değil. Ödeme
düğmesi bizi sandbox alanına götürmüştü, çünkü düğmeyi bu taklit
alanda yaratmıştık.

Şimdi www.sandbox.paypal.com adresine gidersek burada hesabımızdan 10
dolar eksiltildiğini görürüz. Bu tabii ki taklit para. Şimdi satıcı
hesabına girersek orada 10 dolara yakın bir paranın hesabımıza
geçtiğini görüyoruz (10 dolardan biraz eksik olabilir çünkü paypal
komisyonunu kesti).














[1] https://www.youtube.com/watch?v=NFUdd3gveN8

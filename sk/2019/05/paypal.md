# Paypal Odemeleri

Eger normal paypal.com hesabiniz varsa, onunla alakali

https://developer.paypal.com

hesabiniz vardir. Bu adresten bazi gelistirme ve test araclarina
erisebiliyorsunuz. Ustteki adrese paypal kullanici / sifreniz ile
giris yapin.

Burada bazi taklit hesaplar yaratacagiz. Accounts'a tiklayin orada kum
kabi (sandbox) hesaplari yaratmak lazim. Kum kabi icinde kumla oynar
gibi bu taklit hesaplar ile islem gercek islemlere yakin hareketler
yapacagiz.

Bir tane is (business) hesabi yaratin ki bu hesaptan odeme kabul
edebilesiniz, digeri kisisel taklit hesap olsun, ki odeme
gonderebilesiniz. Create Account ile bu hesaplari yaratiriz.

Hesap yaratirken kullanilan email ve sifre ile simdi

sandbox.paypal.com

adresine giris yapabilirsiniz. Bu adres bizi bu taklit kullanicilarin
"hesabina" goturuyor. Bu kullanicilara okkali paypal kullanimi
uzerinden erisebilmek onemli boylece bu hesaplarda oynamalar,
degisiklikler yaparak farkli test senaryolarini deneyebiliriz. 

Bir "satin al" butonu yaratali. Simdi saticinin hesabina girin,
menuden Tools | Business setup secin. Payment setup'tan "on your
website" secip Continue tiklayin. Sonraki sayfada odemeleri nasil
islemek istediginiz soruluyor, sol blok secilebilir, oradaki Continue
secilebilir boylece kredi karti islemi icin dugme / buton
yaratiyoruz. Bir sonraki ekranda "create payment buttons using HTML". 

Dugme tipi icin "Buy Now" tipi yeterli, en basiti bu. Step 2 altinda
"save button at Paypal" isaretini iptal edin. Bir urun fiyati girin,
mesela 10 dolar, simdi create button ile dugmeyi uretin. Dugme bir
HTML kodu olarak size verilecek, bu kodu sitemizin HTML'i icine
koyunca bir odeme dugmesi gorecegiz. Bu dugmeyi bir HTML'e koyun,
sayfayi tarayicida ziyaret edin ve satin alma dugmesini gorun. Ona
tiklayin, paypal'e gideceksiniz, burada taklit alici hesabiyla giris
yapip odemeyi yapin. Odeme sekillerinde kredi kart, paypal hesabi gibi
secenekler var, taklit kredi karti da kullanabilirdik. Son birkac
rakami gosterilen kart gercek degil. Odeme dugmesi bizi sandbox alanina goturmustu, cunku dugmeyi bu taklit
alanda yaratmistik.

Simdi www.sandbox.paypal.com adresine gidersek burada hesabimizdan 10
dolar eksiltildigini goruruz. Bu tabii ki taklit para. Simdi satici
hesabina girersek orada 10 dolara yakin bir paranin hesabimiza
gectigini goruyoruz (10 dolardan biraz eksik olabilir cunku paypal
komisyonunu kesti).














[1] https://www.youtube.com/watch?v=NFUdd3gveN8

Heroku
========

İdare edilen (managed) bulut ortamlarında Google'ın App Engine
yaklaşımına benzer bir yaklaşım Heroku. Gerçi esnek bir sistem
ayarlama şekli var, veri tabanı, Django, onun yerine Flask gibi
yazılımları ortamımıza ekleyebiliyoruz. 

Heroku sanal ortamının işlemci birimi dyno; bir dyno bir sanal Linux
uygulama kabıdır (container). Ölçeklerken "dyno zamanından"
bahsedilir, bir dyno bir saat kullanılmışsa bir dyno saati
tüketilmiştir. Her uygulamaya Heroku tarafından ayda 750 bedava dyno
saati verilir. Bu pek çok basit uygulama için yeterlidir. İsteyenler
ek para ödeyerek daha fazla dyno saati satın alabilirler. Heroku bu
şekilde para kazanmayı umuyor muhakkak.

Not: Dyno'lar 30 dakika kullanılmamışsa uykuya dalar, tekrar uyanırken
biraz yavaşlık olabilir. Belki 10 dakikada bir uygulamayı dışarıdan
"dürterek", ping yaparak uyanık tutmak düşünülebilir.

Üye olmak için 

https://signup.heroku.com/signup/dc

Artık konsola girebiliriz. Heroku ile idare edilen uygulamalarımız,
projelerimiz var, bu projeleri istediğimiz gibi ölçekleyebiliyoruz,
onlara kaynak yöneltebiliyoruz. Mevcut uygulamaları görmek için 

https://dashboard.heroku.com/apps

Mevcut bir app'i silmek için app ismine tıklanır, Settings kısmında en
altta "Delete app" düğmesine basılıp app ismi bir daha girilip silme
yapılır. Yeni app için Create New App seçin. isim global olarak özgün
olmalı. Yerel bilgisayarda (daha önce kurulmadıysa bir kerelik)

```
sudo snap install heroku --classic
```

Simdi,

```
heroku login
```

dedikten sonra soruda enter'e basın, tarayıcıya gidiyor, bu geçici, iş
bittikten sonra tarayıcı kapatılıp komut satırına dönülebilir. Biraz
garip bir giriş yapma şekli ama işliyor.

Bir uygulama kuralım. Alttaki repo'da daha önce yaratılmış çok basit
bir Heroku uygulaması var. 

```
git clone git@github.com:franccesco/flask-heroku-example.git

cd flask-heroku-example
```

Ya da sıfırdan bir dizin içinde yeni kodlar koyup onları `git init`,
ve `git add` ile bir repo'ya dahil edebilirdik. Sonuç aynı. Simdi,

```
heroku apps:create flask-heroku-example-[bir şeyler, özgün isim olsun diye]
```

ile Heroku projesi yaratılır. Bu noktada hala app sonuç ortamına
gönderilmedi. Sayfanızı ziyaret ederseniz,

```
Heroku | Welcome to your new app!
```

mesajını görürsünüz.

```
git push heroku master
```

ile kod gönderin. Eğer problem çıkarsa `.git/config` içinde

```
url = https://git.heroku.com/flask-microblog-[...].git
```

olduğunu kontrol edin. Ve `git push` tekrarlayın.

Mimari açıdan bilinmesi gereken önemli bir faktör `push` yaptığımızda
Heroku'nun servisleri tekrar başlattığı... Bu sırada birkaç işlem
oluyor, repo'daki kod paketlenir, repo ile bağlı olan uygulama bulunup
onun dyno'ları indirilip yeni kodla tekrar başlatılır. Bu önemli,
çünkü mesela diyelim ki `static` altında sadece birkaç ufak sayfa
değiştirdik, bunları göndersek `push` bunu anlayıp servisleri tekrar
başlatmayabilirdi belki diye düşünebilirdik. Bu doğru değil. Bu tekrar
başlatma sırasında kullanıcı sisteme ufak bir süre erisemeyebilir, bu
sebeple, mesela bir içerik idare sistemi yazıyorsak bunu repo
üzerinden yapmamak gerekir. 


```
heroku open
```

ile tarayıcıyı direk uygulamanın işlediği URL'i ziyared edecek şekilde
açabiliyoruz.

Heroku'nun çok Github repo merkezli işlediğini farketmişizdir
herhalde. Kod gönderirken repo dizini içinde olmak lazım, `heroku
open` deyince o repo ile alakalı olan URL biliniyor, vs. Yani Heroku
uygulaması ile repo birbiri ile alakalı hale geliyor. Sistemleri böyle
işliyor.

Ölçekleme

```
heroku ps:scale web=1
```

```
Scaling dynos... done, now running web at 1:Free
```

`web=2` diyebilirdik.

Ayarlar

Üstte gösterilen örnek Python bazlı projeydi. Bu tür projelerin (ve
genel Heroku projelerinin) ayarı için `Pipfile` ve `Procfile`
dosyaları var. Bu dosyalar Heroku proje dizininde en üstte
görülebiliyor. Dosya `runtime.txt` içinde hangi python versiyonu
istediğimiz seçilebilir.

Dosya Sistemi

Dikkat: idare edilen bir ortam olduğu için Heroku "disk" ve "dosya
sistemi" erişimini garanti etmiyor. Daha doğrusu bir dosya sistemi var
ama bu sistem birdenbire değişip bir başka makinanın sistemine
dönüşebiliyor (herhalde arka planda yük dağıtımı yaparken kod bir o
bir makinaya kaydırılıyor). Bu yüzden Heroku disk sistemi için "uçucu
(ephemeral)" deniyor. Diske yazılan bir şeyin orada kalacağına
güvenmemek lazım. Kalıcı olmasını istediğimiz şeyleri Heroku
tarafından desteklenen Posgresql tabanına yazmak lazım.

Log

Geliştirme makinanızda `heroku logs --tail` ile nihai ortamdaki
`print` ve benzeri komutlarının çıktısını takip edebilirsiniz.

Python Paket Kullanımı

Eğer servis içinde python'un kullanması gereken ek paketler varsa,
bunları `Pipfile` içine koyabiliriz, mesela ek iki paket olarak

```
...
requests = "*"
urllib3 = "*"
```

ekleyebilirdim. Eğer versiyon numarasını da vermek istersem, mesela

```
bcrypt = ">=1.1"
cryptography = "==2.3"
```

kullanımı işliyor. Bu arada eğer varsa `Pipfile.lock` dosyasını
silin. Eğer problem çıkarsa uygulamanızı silip tekrar yaratın. Not:
İnternet'te `requirements.txt` kullanımı ile ilgili bazı tavsiyeler
var ama bunlar işlemiyor.

Uygulamamızın kaç bedava saati kaldığını görmek için

```
heroku ps -a [uygulama ismi]
```

Ornek sonuc

```
Free dyno hours quota remaining this month: 546h 5m (99%)
Free dyno usage for this app: 0h 55m (0%)
For more information on dyno sleeping and how to upgrade, see:
https://devcenter.heroku.com/articles/dyno-sleeping

=== web (Free): gunicorn app:app (1)
web.1: up 2019/05/27 12:19:55 +0300 (~ -172s ago)
```

Veri Tabani Eklemek

Uygulamamıza bir servis olarak bir Postgresql tabanı "ekleyebiliriz".

https://data.heroku.com/

gidiyoruz, seçeneklerden Heroku Postgres seçiyoruz, "Create one"
tıklıyoruz, sonraki ekranda "Install Heroku Postgres"'e
tıklıyoruz. Sonraki pencerede PG'nin hangi uygulamaya atanacağı (app
to provision to) soruluyor, bu kutuya istediğimiz uygulamanın ismini
yazıyoruz. Seçip "Provision add-on" diyoruz. Belli PG seviyeleri var,
bedava en az kapsamlı olan "Hobby Dev" seviyesi. Olağan değer bu
olacaktır zaten.

PG eklendikten sonra tabana tıklarız, çıkan ekranda Settings ve View
Credentials ile tabana erişmek için gereken makina, taban ismi,
kullanıcı, vs bilgileri görebiliriz.


[1] https://blog.miguelgrinberg.com/post/the-flask-mega-tutorial-part-xviii-deployment-on-heroku

[2] https://devcenter.heroku.com/articles/getting-started-with-python

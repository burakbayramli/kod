Heroku
========

İdare edilen (managed) bulut ortamlarında Google'ın App Engine
yaklaşımına benzer bir yaklaşım Heroku. Gerçi esnek bir sistem
ayarlama şekli var, veri tabanı, Django, onun yerine Flask gibi
yazılımları ortamımıza ekleyebiliyoruz. 

Heroku sanal ortamının işlemci birimi dyno; bir dyno bir sanal Linux
uygulama kabidir (container). Ölçeklerken "dyno zamanından"
bahsedilir, bir dyno bir saat kullanılmışsa bir dyno saati
tüketilmiştir. Her uygulamaya Heroku tarafından 750 bedava dyno saati
verilir. Bu pek çok basit uygulama için yeterlidir. İsteyenler ek para
ödeyerek daha fazla dyno saati satın alabilirler. Heroku bu şekilde
para kazanmayı umuyor muhakkak. 

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

heroku apps:create flask-heroku-example-[bir seyler, ozgun isim olsun diye]
```

Bu noktada hala app sonuç ortamına gönderilmedi. Sayfanızı ziyaret ederseniz,

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

```
heroku open
```

ile tarayıcıyı direk uygulamanın işlediği URL'i ziyared edecek şekilde
açabiliyoruz.

Heroku'nun çok Github repo merkezli işlediğini farketmişizdir
herhalde. Kod gönderirken repo dizini içinde olmak lazım, `open`
deyince o repo ile alakalı olan ÜRL biliniyor, vs. Sistemleri böyle işliyor.

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

Gelistirme makinanizda `heroku logs --tail` ile nihai ortamdaki
`print` ve benzeri komutlarinin ciktisini takip edebilirsiniz.

Python Paket Kullanımı

Eğer servis içinde python'un kullanması gereken ek paketler varsa,
bunları `Pipfile` içine koyabiliriz, mesela ek iki paket olarak

```
...
requests = "*"
urllib3 = "*"
```

ekleyebilirdim. Bu arada eğer varsa `Pipfile.lock` dosyasını
silin. Eğer problem çıkarsa uygulamanızı silip tekrar yaratın. Not:
İnternet'te `requirements.txt` kullanımı ile ilgili bazı tavsiyeler
ama bunlar işlemiyor.


Bu yazıya ekler olabilir

[1] https://blog.miguelgrinberg.com/post/the-flask-mega-tutorial-part-xviii-deployment-on-heroku

[2] https://devcenter.heroku.com/articles/getting-started-with-python

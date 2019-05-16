heroku
========

https://devcenter.heroku.com/articles/getting-started-with-python

https://signup.heroku.com/signup/dc

https://dashboard.heroku.com/apps

Create New App
isim ozgun olmali.

yerel bilgisayarda
sudo snap install heroku --classic

heroku login
dedikten sonra soruda enter'e basin, tarayiciya gidiyor, bu gecici, is
bittikten sonra tarayici kapatilip komut satirina donulebilir.

git clone https://github.com/heroku/python-getting-started.git

https://protected-reef-81845.herokuapp.com/ | https://git.heroku.com/protected-reef-81845.git

heroku ps:scale web=1

Scaling dynos... done, now running web at 1:Free

heroku open
https://protected-reef-81845.herokuapp.com/

https://blog.miguelgrinberg.com/post/the-flask-mega-tutorial-part-xviii-deployment-on-heroku

git clone https://github.com/miguelgrinberg/microblog

cd microblog

git checkout v0.18

heroku apps:create flask-microblog-10

Bu noktada hala app sonuc ortamina gonderilmedi. Sayfanizi ziyaret ederseniz,

Heroku | Welcome to your new app!

mesajini gorursunuz.

git push heroku master

ile kod gonderin. Eger problem cikarsa .git/config icinde

url = https://git.heroku.com/flask-microblog-10.git

oldugunu kontrol edin. Ve `git push` tekrarlayin. 




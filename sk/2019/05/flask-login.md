# Flask, Kullanıcı Giriş (Login), Şifre, Oturum İdaresi, Flask-User

Kullanıcının şifresini almak, unuttuysa hatırlatmak, email üzerinden
kaydetmek, oturum açıp oturumu hatırlamak, hangi sayfaya izin var
hangisine yok kontrol etmek, vs. gibi işlerlerle gayet "Pythonic"
şekilde başedebilmemizi sağlayan bir paket Flask-User. Kodları,

https://github.com/lingthio/Flask-User/

dan indirebiliriz. Gerekli paketleri 

```
pip install -r requirements.txt
```

ile kurarız. Örnek bir kod ([1] baz alınmıştır), `basic_app.py` diyelim,

```
import datetime, os, util
from flask import Flask, request, render_template
from flask_babelex import Babel
from flask_sqlalchemy import SQLAlchemy
from flask_user import current_user, login_required
from flask_user import roles_required, UserManager, UserMixin

class ConfigClass(object):
    """ Flask application config """

    SECRET_KEY = 'This is an INSECURE secret!! DO NOT use this in production!!'

    SQLALCHEMY_DATABASE_URI = 'sqlite:///basic_app.sqlite' 
    SQLALCHEMY_TRACK_MODIFICATIONS = False   

    MAIL_SERVER = 'smtp.gmail.com'
    MAIL_PORT = 587
    MAIL_USE_SSL = False
    MAIL_USE_TLS = True
    MAIL_USERNAME = '[kaynak email@gmail.com]'
    MAIL_PASSWORD = '[sifre]'
    MAIL_DEFAULT_SENDER = '"MyApp" <noreply@example.com>'

    USER_APP_NAME = "Flask-User Basic App"
    USER_ENABLE_EMAIL = True       
    USER_ENABLE_USERNAME = False  
    USER_EMAIL_SENDER_NAME = USER_APP_NAME
    USER_EMAIL_SENDER_EMAIL = "noreply@example.com"


def create_app():
    """ Flask application factory """
    
    app = Flask(__name__)
    app.config.from_object(__name__+'.ConfigClass')

    babel = Babel(app)

    db = SQLAlchemy(app)

    class User(db.Model, UserMixin):
        __tablename__ = 'users'
        id = db.Column(db.Integer, primary_key=True)
        active = db.Column('is_active', db.Boolean(),
	                   nullable=False, server_default='1')

        email = db.Column(db.String(255, collation='NOCASE'),
	                  nullable=False, unique=True)
        email_confirmed_at = db.Column(db.DateTime())
        password = db.Column(db.String(255), nullable=False, server_default='')

        first_name = db.Column(db.String(100, collation='NOCASE'),
	                       nullable=False, server_default='')
        last_name = db.Column(db.String(100, collation='NOCASE'),
	                      nullable=False, server_default='')

        roles = db.relationship('Role', secondary='user_roles')

    class Role(db.Model):
        __tablename__ = 'roles'
        id = db.Column(db.Integer(), primary_key=True)
        name = db.Column(db.String(50), unique=True)

    class UserRoles(db.Model):
        __tablename__ = 'user_roles'
        id = db.Column(db.Integer(), primary_key=True)
        user_id = db.Column(db.Integer(),
	                    db.ForeignKey('users.id', ondelete='CASCADE'))
        role_id = db.Column(db.Integer(),
	                    db.ForeignKey('roles.id', ondelete='CASCADE'))

    user_manager = UserManager(app, db, User)

    db.create_all()

    if not User.query.filter(User.email == 'member@example.com').first():
        user = User(
            email='member@example.com',
            email_confirmed_at=datetime.datetime.utcnow(),
            password=user_manager.hash_password('Password1'),
        )
        db.session.add(user)
        db.session.commit()

    if not User.query.filter(User.email == 'admin@example.com').first():
        user = User(
            email='admin@example.com',
            email_confirmed_at=datetime.datetime.utcnow(),
            password=user_manager.hash_password('Password1'),
        )
        user.roles.append(Role(name='Admin'))
        user.roles.append(Role(name='Agent'))
        db.session.add(user)
        db.session.commit()

    @app.route('/')
    def home_page():
        return render_template("home.html")

    @app.route('/members')
    @login_required    # Use of @login_required decorator
    def member_page():
        return render_template("/member_page.html")

    @app.route('/admin')
    @roles_required('Admin')    # Use of @roles_required decorator
    def admin_page():
        return render_template("/admin.html")

    return app

if __name__ == '__main__':
    app = create_app()
    app.run(host='0.0.0.0', port=5000, debug=True)

```

Sayfalar `templates` alt dizininde,

admin.html

```
{% extends "flask_user_layout.html" %}
{% block content %}
<h2>{%trans%}Admin Page{%endtrans%}</h2>
<p><a href={{ url_for('user.register') }}>{%trans%}Register{%endtrans%}</a></p>
<p><a href={{ url_for('user.login') }}>{%trans%}Sign in{%endtrans%}</a></p>
<p><a href={{ url_for('home_page') }}>{%trans%}Home Page{%endtrans%}</a> </p>
<p><a href={{ url_for('member_page') }}>{%trans%}Member Page{%endtrans%}</a> </p>
<p><a href={{ url_for('admin_page') }}>{%trans%}Admin Page{%endtrans%}</a> </p>
<p><a href={{ url_for('user.logout') }}>{%trans%}Sign out{%endtrans%}</a></p>
{% endblock %}
```

home.html

```
{% extends "flask_user_layout.html" %}
{% block content %}
<h2>{%trans%}Home page{%endtrans%}</h2>
<p><a href={{ url_for('user.register') }}>{%trans%}Register{%endtrans%}</a></p>
<p><a href={{ url_for('user.login') }}>{%trans%}Sign in{%endtrans%}</a></p>
<p><a href={{ url_for('home_page') }}>{%trans%}Home Page{%endtrans%}</a> </p>
<p><a href={{ url_for('member_page') }}>{%trans%}Member Page{%endtrans%}</a> </p>
<p><a href={{ url_for('admin_page') }}>{%trans%}Admin Page{%endtrans%}</a> </p>
<p><a href={{ url_for('user.logout') }}>{%trans%}Sign out{%endtrans%}</a></p>
{% endblock %}
```
member_page.html

```
{% extends "flask_user_layout.html" %}
{% block content %}
<h2>{%trans%}Members page{%endtrans%}</h2>
<p><a href={{ url_for('user.register') }}>{%trans%}Register{%endtrans%}</a></p>
<p><a href={{ url_for('user.login') }}>{%trans%}Sign in{%endtrans%}</a></p>
<p><a href={{ url_for('home_page') }}>{%trans%}Home Page{%endtrans%}</a> </p>
<p><a href={{ url_for('member_page') }}>{%trans%}Member Page{%endtrans%}</a> </p>
<p><a href={{ url_for('admin_page') }}>{%trans%}Admin Page{%endtrans%}</a> </p>
<p><a href={{ url_for('user.logout') }}>{%trans%}Sign out{%endtrans%}</a></p>
{% endblock %}
```

Bu kod basit bir şekilde `python basic_app.py` diye başlatılır. Email,
sifre girilir, ve konfirmasyon için email gelir, ona tıklanır,
kullanıcı doğrulanmış olur.

Eğer bir sayfayı (onun metoduna daha doğrusu) site giriş yapmış
olanlar için kısıtlamak istersek o metotu `@login_required` ile
işaretleriz.

Kullanıcılar çok basit bir sqlite veri tabanında tek bir dosya içinde
kaydedilirler, dosya ismi `SQLALCHEMY_DATABASE_URİ` ile tanımlı. 

Giriş yapmak, oturum kapatmak, şifre ile ilgili envai türden işlem
üstteki örnek ile yapılabiliyor.

SQL

Flask-User içinde SQLAlchemy kullanılıyor, bu bir tür ORM
(ilişkisel-obje eşlemesi), Java dünyasındaki Hibernate gibi. Fakat siz
direk SQL ile iş yapmak isterseniz,

```
res = db.engine.execute('select * from users')
for x in res: print (x)
```

kullanımı olur. `db` referansı `db = SQLAlchemy(app)` ifadesinden geliyor. 



Email, Gmail SMTP ile ilgili problem olursa bir [diğer yazı](/2012/06/python-ile-mail-gondermek-smtp-gmail.html). 

[1] https://github.com/lingthio/Flask-User/blob/master/example_apps/basic_app.py
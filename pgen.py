import string
import secrets
alphabet = string.ascii_letters + string.digits + '-_'
while True:
    password = ''.join(secrets.choice(alphabet) for i in range(20))
    if (sum(c.islower() for c in password) >=2
            and sum(c.isupper() for c in password) >=2
            and sum(c.isdigit() for c in password) >=2):
        break
    
print (password)

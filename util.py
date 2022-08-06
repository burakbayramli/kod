import string, secrets, os, binascii
import hashlib, subprocess 

def pgen():
    alphabet = string.ascii_letters + string.digits + '-_'
    while True:
        password = ''.join(secrets.choice(alphabet) for i in range(20))
        if (sum(c.islower() for c in password) >=2
                and sum(c.isupper() for c in password) >=2
                and sum(c.isdigit() for c in password) >=2):
            break
    return password

def my_random(upper):    
    p = subprocess.Popen(['ps','gaux'], stdout=subprocess.PIPE)
    res = p.stdout.read()
    r = hashlib.md5(res)
    return int(r.hexdigest(),16) % upper

    
if __name__ == "__main__":
    
    print (my_random(100))
    print (pgen())

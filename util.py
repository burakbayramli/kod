import string, secrets, os, binascii, time
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
    a = int(r.hexdigest(),16)
    b = int(time.time())
    c = int(binascii.hexlify(os.urandom(10)), 16) 
    res = int(str(a) + str(b) + str(c))    
    return res % upper

    
if __name__ == "__main__":
    
    print (my_random(100))
    #print (pgen())

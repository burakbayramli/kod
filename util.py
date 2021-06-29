import string, secrets, random
import time, uuid, os, binascii

def pgen():
    alphabet = string.ascii_letters + string.digits + '-_'
    while True:
        password = ''.join(secrets.choice(alphabet) for i in range(20))
        if (sum(c.islower() for c in password) >=2
                and sum(c.isupper() for c in password) >=2
                and sum(c.isdigit() for c in password) >=2):
            break

    print (password)


def my_random(upper):    
    rand = os.urandom(10)
    m =  str(int(time.time() * 1000))
    m += str(int(uuid.uuid4().int / 1e30))
    m += str(int(random.random() * 1e7))
    m +=  str(int(time.time() * 100))
    m += str(int(binascii.hexlify(rand), 16))
    
    return (int(m)  % upper)

    
if __name__ == "__main__":
    
    print (my_random(100))

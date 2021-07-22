import string, secrets, os, binascii

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
    rand = os.urandom(10)
    m = str(int(binascii.hexlify(rand), 16))    
    return (int(m)  % upper)

    
if __name__ == "__main__":
    
    print (my_random(100))
    print (pgen())

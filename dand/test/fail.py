import time, random, logging, os

logging.basicConfig(filename=os.environ['TEMP']+'/example.log',level=logging.DEBUG)

for i in range(5):
    logging.debug('trying...')
    time.sleep(1)
exit(-1)
    

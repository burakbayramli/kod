import subprocess, threading, datetime, time
import yaml, os, sys, glob, logging
from subprocess import Popen, PIPE

import numpy as np

def today():
    today=datetime.datetime.today()
    return today.year, today.month, today.day, today.hour, today.minute, today.strftime("%A")
    
def execute(cmd, name):
    '''
    Run the program in a seperate process
    '''
    returncode = subprocess.call(cmd, shell=True)
    return returncode

def run(conf,name):
    '''
    Run the program, watch the output, restart if necessary
    '''
    restart_curr = 0
    while (True):
        returncode = execute(conf['exec'], name)
        if returncode:
            logging.debug('Failure with returncode %d, for %s' % (returncode,name))
            if 'restart' in conf.keys() and 'forever' in str(conf['restart']):
                logging.debug('restarting...bcz it says forever, %s' % conf['exec'])
                continue
            if 'restart' in conf.keys() and restart_curr < int(conf['restart']):
                restart_curr += 1
                logging.debug('restarting...%s' % conf['exec'])
                logging.debug('at restart count...%d' % restart_curr)
                continue
            else:
                break
        else:
            break
                
def runner(name, conf):
    """
    Parallel runner for each item in the configuration
    """
    logging.debug('Running %s %s' % (name, conf['exec']))
    try:
        # keeps track of having ran the program once for the scheduled
        # time. if we ran the program for Wednesday, we don't want to
        # run it every two seconds in that Wednesday.
        ran_once = False
        if 'schedule' in conf.keys():
            while True:            
                (tyear,tmonth,tday,thour,tminute,tweekday) = today()
                a = ['year' in conf['schedule'], 'month' in conf['schedule'], 'day' in conf['schedule'], \
                     'hour' in conf['schedule'], 'minute' in conf['schedule'], 'weekday' in conf['schedule']]
                b = [conf['schedule'].get('year') == tyear, conf['schedule'].get('month') == tmonth,\
                     conf['schedule'].get('day') == tday, conf['schedule'].get('hour') == thour, \
                     conf['schedule'].get('minute') == tminute, conf['schedule'].get('weekday') == tweekday]

                # If for every existing key in the schedule, we have a
                # match, then the current time is a match for this
                # schedule. The boolean array usage can be confusing,
                # but all I'm doing is for keys x,y,z if the existence
                # of them are [True, True, False] and I have [Blah,
                # Blah, Blah] on current time match, then keys AND'ed
                # with current time match must give me exactly the
                # same boolean array as the key check, that is, [True,
                # True, False].
                if a == list(np.array(a) & np.array(b)):
                    if ran_once == False: 
                        # run the program
                        logging.debug('schedule %s is a match' % conf['schedule'])
                        run(conf,name)
                        ran_once = True                    
                else:
                    # we are out of the match condition, and if we already ran
                    # once, now time to go back to the way things were - 
                    # as if we never ran before, and we are waiting for the
                    # schedule match.
                    if ran_once == True: ran_once = False
                    time.sleep(2)

                    
                # in case of scheduling, always continue, never
                # finish, because we might be on a daily, weekly
                # schedule, we might need to run the program again for
                # the next time the schedule condition matches.
                continue 
        else:
            # simply run the program
            run(conf,name)                
            
    except OSError, message:
        logging.debug('Execution failed! \n %s \n' % message)
        sys.exit(1)

if __name__ == "__main__": 

    if len(sys.argv) != 2: print "Usage: dandy.py [conf file]"; exit(0)
    log_file = sys.argv[1].replace(".conf","").replace("/","_")
    log_file = log_file.replace("-","_")
    flog = os.environ['TEMP'] + '/dand_%s.log' % log_file
    print 'log file', flog
    logging.basicConfig(filename=flog,level=logging.DEBUG,format='%(asctime)-15s: %(message)s')   
    f = open(sys.argv[1], "r")

    conf = yaml.load(f)
    for process in conf.keys():
        t = threading.Thread(target=runner, args=(process, conf[process],))
        t.start()
                    

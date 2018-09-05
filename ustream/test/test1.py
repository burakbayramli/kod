from threading import Thread
import sys, time, zmq

def receive():
    context = zmq.Context()
    receiver = context.socket(zmq.PULL)
    receiver.bind("tcp://*:10000")

    arr = []
    while True:
        data = receiver.recv()
        if "exit" in data: exit()
        print '-----------'
        print data

if __name__ == "__main__": 
         
    Thread(target=receive).start()

    context = zmq.Context()
    sender = context.socket(zmq.PUSH)
    sender.connect("tcp://localhost:10000")
    for i in range(100):
        sender.send(str(i))
    sender.send("exit")
    exit()

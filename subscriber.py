import zmq
import json
import random
import time
from uuid import uuid4
from datetime import datetime


host = "127.0.0.1"
port = "8888"

context = zmq.Context()

subscriber = context.socket(zmq.SUB)

subscriber.connect("tcp://"+ host +":" + port )
subscriber.setsockopt(zmq.SUBSCRIBE, b"")
#subscriber.setsockopt(zmq.SUBSCRIBE, b"answer")

while True:
        # Read envelope with address
        [address, contents] = subscriber.recv_multipart()
        print("[%s] %s" % (address, contents))



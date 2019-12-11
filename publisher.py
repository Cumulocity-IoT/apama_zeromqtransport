import zmq
import json
import random
import time
from uuid import uuid4
from datetime import datetime

topic = "weather"
host = "127.0.0.1"
port = "7777"

context = zmq.Context()

sock = context.socket(zmq.PUB)

sock.bind("tcp://"+ host +":" + port )


while True:
    message_id = str(uuid4()) 
    now =  datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    humidity = random.randrange(20, 40)
    temperature_in_celsius = random.randrange(32, 41)

    payload = json.dumps({"message_id": message_id, "humidity":humidity, "temperature_in_celsius":temperature_in_celsius, "createdAt":now})
    sock.send_multipart([bytes(topic,encoding='utf-8'),bytes(payload,encoding='utf-8')])
    time.sleep(1)
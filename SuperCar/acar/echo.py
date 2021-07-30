#!/usr/bin/python3

import RPi.GPIO as GPIO
import time

GPIO.setmode(GPIO.BCM)


GPIO_TRIGGER = 4
GPIO_ECHO = 17

GPIO.setup(GPIO_TRIGGER, GPIO.OUT)
GPIO.setup(GPIO_ECHO, GPIO.IN)

def distance():
    GPIO.output(GPIO_TRIGGER, True)
    time.sleep(0.00001)
    GPIO.output(GPIO_TRIGGER, False)
    start_time = time.time()
    stop_time = time.time()
    while GPIO.input(GPIO_ECHO) == 0:
        start_time = time.time()
    while GPIO.input(GPIO_ECHO) == 1:
        stop_time = time.time()
    time_elapsed = stop_time - start_time
    distance = (time_elapsed * 34300) / 2
    return distance


if __name__=='__main__':
    try:
        while True:
            dist = distance()
            print("distance = {:.2f} cm".format(dist))
            time.sleep(1)
    except KeyboardInterrupt:
        print("stop")
        GPIO.cleanup()


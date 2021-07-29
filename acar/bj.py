#!/usr/bin/python3

import RPi.GPIO as GPIO
import time
GPIO.setmode(GPIO.BCM)


def init():
    p1 = 6
    p2 = 13
    p3 = 19
    p4 = 26
    GPIO.setup(p1, GPIO.OUT, initial=GPIO.LOW)
    GPIO.setup(p2, GPIO.OUT, initial=GPIO.LOW)
    GPIO.setup(p3, GPIO.OUT, initial=GPIO.LOW)
    GPIO.setup(p4, GPIO.OUT, initial=GPIO.LOW)
    print("init  port", p1,p2,p3,p4)
    return [p1,p2,p3,p4, p1]

pins = init()

def bjtest(pin1, delay):
    for i in range(4):
        GPIO.output(pins[i], True)
        time.sleep(delay)
        GPIO.output(pins[i], False)


if __name__ == '__main__':
    try:
        i = 0
        while True:
            if (i<400):
                bjtest(i%4, 0.002)
            elif (i<800):
                bjtest(i%4, 0.0018)
            else:
                bjtest(i%4, 0.0015)
            i = i+ 1
#            r=input("test")
#            time.sleep(0.005)
    except KeyboardInterrupt:
        print("Measurement stopped by User")
        GPIO.cleanup()

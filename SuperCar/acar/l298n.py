import RPi.GPIO as GPIO
import time

GPIO.setmode(GPIO.BCM)

pin1 = 6
pin2 = 13
pin3 = 19
pin4 = 26

#GPIO.setup(Vcc_Pin,GPIO.OUT,initial=GPIO.HIGH)
GPIO.setup(pin1, GPIO.OUT, initial=GPIO.LOW)
GPIO.setup(pin2, GPIO.OUT, initial=GPIO.LOW)
GPIO.setup(pin3, GPIO.OUT, initial=GPIO.LOW)
GPIO.setup(pin4, GPIO.OUT, initial=GPIO.LOW)

p1 = GPIO.PWM(pin1, 50)
p2 = GPIO.PWM(pin2, 50)
p3 = GPIO.PWM(pin3, 50)
p4 = GPIO.PWM(pin4, 50)

p1.start(0)
p2.start(0)
p3.start(0)
p4.start(0)

str1="please input the degree(0<=a<=120)\nor press q to quit\n"
r=input(str1)
try:
    while not r=="q":
        if r.isdigit():
            r=int(r)
        else:
            print("please input a number(0<=num<=120)")
            continue
        if r<0 or r>180:
            print("a must be [0,120]")
            continue
        print("set to ", r)
        if r > 50 :
            p1.ChangeDutyCycle(0)
            p2.ChangeDutyCycle(r-50)
            p3.ChangeDutyCycle(0)
            p4.ChangeDutyCycle(r-50)
        else:
            p1.ChangeDutyCycle(r)
            p2.ChangeDutyCycle(0)
            p3.ChangeDutyCycle(r)
            p4.ChangeDutyCycle(0)

        time.sleep(0.02)
        r=str(input(str1))
except KeyboardInterrupt:
    pass
p1.stop()
p2.stop()
p3.stop()
p4.stop()
GPIO.cleanup()

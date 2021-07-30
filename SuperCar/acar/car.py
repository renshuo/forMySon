
import RPi.GPIO as GPIO
import time
GPIO.setmode(GPIO.BCM)
GPIO_TRIGGER = 4
GPIO_ECHO = 17
GPIO.setup(GPIO_TRIGGER, GPIO.OUT)
GPIO.setup(GPIO_ECHO, GPIO.IN)

r=40

def init():
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
    print("init L298N port", p1,p2,p3,p4)
    return [p1,p2,p3,p4]

p1,p2,p3,p4 = init()

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

def backward():
    p1.ChangeDutyCycle(0)
    p2.ChangeDutyCycle(r)
    p3.ChangeDutyCycle(0)
    p4.ChangeDutyCycle(r)

def forward():
    p1.ChangeDutyCycle(r)
    p2.ChangeDutyCycle(0)
    p3.ChangeDutyCycle(r)
    p4.ChangeDutyCycle(0)

def toLeft():
    p1.ChangeDutyCycle(0)
    p2.ChangeDutyCycle(r)
    p3.ChangeDutyCycle(r)
    p4.ChangeDutyCycle(0)

def toRight():
    p1.ChangeDutyCycle(r)
    p2.ChangeDutyCycle(0)
    p3.ChangeDutyCycle(0)
    p4.ChangeDutyCycle(r)

def stop():
    p1.ChangeDutyCycle(0)
    p2.ChangeDutyCycle(0)
    p3.ChangeDutyCycle(0)
    p4.ChangeDutyCycle(0)

forward()
if __name__ == '__main__':
    try:
        while True:
            dist = distance()
            if dist<15:
                print("distance < 15, backward")
                stop()
                time.sleep(2)
                backward()
                time.sleep(1.5)
                stop()
                time.sleep(2)
                toLeft()
                time.sleep(1)
                stop()
                time.sleep(2)
                forward()

            print("Measured Distance = {:.2f} cm".format(dist))
            time.sleep(1)
  
        # Reset by pressing CTRL + C
    except KeyboardInterrupt:
        print("Measurement stopped by User")
        GPIO.cleanup()

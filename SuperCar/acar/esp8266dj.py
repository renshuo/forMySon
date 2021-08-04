import network

ssid = 'Xiaomi_AA96'
password = '831130ren'

wifi_status = network.WLAN(network.STA_IF)
wifi_status.active(True)
wifi_status.connect(ssid, password)

# check wifi connected
while wifi_status.isconnected() == False:
    print('Wifi lost connect...')

# if connected
print('Wifi connect successful')
print(wifi_status.ifconfig())

# ======================
print("Hello world")
import time
from machine import Pin, PWM
freq = 50
initDuty = 25
p2 = PWM(Pin(5), freq, initDuty)
time.sleep(0.4)

for i in range(25, 125):
    p2.duty(i)
    print(i)
    time.sleep(0.03)

p2.duty(0)

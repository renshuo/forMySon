package rs.dev

import com.pi4j.io.gpio._
import com.pi4j.io.gpio.event.{GpioPinDigitalStateChangeEvent, GpioPinListener, GpioPinListenerDigital}

class GpioDev {


  val gpio: GpioController = try{
    val gpio = GpioFactory.getInstance()
    com.pi4j.wiringpi.Gpio.pwmSetMode(com.pi4j.wiringpi.Gpio.PWM_MODE_MS)
    /**
     * PI的PWM频率计算方法：
     * pi的PWM频率是 19.2MHz 即 19200_000
     * 所以 19200_000 = Range * ClockDiver * Rate
     * 假设要做50Hz的PWM, rate = 50, Range是setPwm的取值范围，假设取 1-1000
     * 则 clockDiver = 19200_000 / 1000 / 50 = 384
     */
    com.pi4j.wiringpi.Gpio.pwmSetRange(1000)
    com.pi4j.wiringpi.Gpio.pwmSetClock(384)
    gpio
  }catch {
    case any => println(s"gpio error: ${any}")
    null
  }
}

class GpioDevPwm(port: Int) extends GpioDev {
  val dev = gpio.provisionPwmOutputPin(RaspiPin.getPinByAddress(port), 0)

  def setPwm(rate: Double): Unit = {
    dev.setPwm((25.0 + rate.toFloat * 100.0 / 180.0).ceil.toInt)
  }
}

class GpioDevDigitalOut(port: Int) extends GpioDev {
  val dev = gpio.provisionDigitalOutputPin(RaspiPin.getPinByAddress(port), "", PinState.LOW)

  def low = dev.low()
  def high = dev.high()
  def toggle = dev.toggle()
}

class GpioDevDigitalIn(port: Int) extends GpioDev {
  val dev = gpio.provisionDigitalInputPin(RaspiPin.getPinByAddress(port), "")

  def getState = dev.getState

  def isHigh = dev.getState == PinState.HIGH
  def isLow = dev.getState == PinState.LOW
}

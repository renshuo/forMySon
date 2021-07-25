import com.pi4j.io.gpio._
import com.pi4j.io.gpio.event.{GpioPinDigitalStateChangeEvent, GpioPinListener, GpioPinListenerDigital}
import com.pi4j.io.gpio.trigger.GpioCallbackTrigger

import scala.io.StdIn

val gpio: GpioController  = GpioFactory.getInstance()

val fv = 50

class Leg (p1: Pin, p2: Pin) {
  val p1out: GpioPinPwmOutput = gpio.provisionSoftPwmOutputPin(p1)
  val p2out: GpioPinPwmOutput = gpio.provisionSoftPwmOutputPin(p2)

  def forward = { p1out.setPwm(fv); p2out.setPwm(0) }

  def backward = { p1out.setPwm(0); p2out.setPwm(fv) }

  def stop = { p1out.setPwm(0); p2out.setPwm(0) }
}

object car {
  val fl = new Leg(RaspiPin.GPIO_07, RaspiPin.GPIO_00)
  val fr = new Leg(RaspiPin.GPIO_03, RaspiPin.GPIO_02)
  val bl = new Leg(RaspiPin.GPIO_25, RaspiPin.GPIO_29)
  val br = new Leg(RaspiPin.GPIO_27, RaspiPin.GPIO_28)


  def forward = { fl.forward ; fr.forward; bl.forward; br.forward }

  def turnLeft = { fl.stop; bl.stop; fr.forward; br.forward}

  def turnRight = { fl.forward; bl.forward; fr.stop; br.stop }

  def backward = { fl.backward; fr.backward; bl.backward; br.backward }

  def stop = { fl.stop; bl.stop; fr.stop; br.stop}

  def test = {
    this.stop
    fl.forward
    StdIn.readLine()
    fl.stop
    fr.forward
    StdIn.readLine()
    fr.stop
    bl.forward
    StdIn.readLine()
    bl.stop
    br.forward
    StdIn.readLine()
    this.stop
  }
}

@main def TestCar() = {

  car.test
}
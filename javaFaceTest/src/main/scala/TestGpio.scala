

import com.pi4j.io.gpio._

val gpio: GpioController  = GpioFactory.getInstance()

val fv = 80

class Leg (p1: Pin, p2: Pin) {
  val p1out: GpioPinPwmOutput = gpio.provisionPwmOutputPin(p1)
  val p2out: GpioPinPwmOutput = gpio.provisionPwmOutputPin(p2)

  def forward = { p1out.setPwm(fv); p2out.setPwm(0) }

  def backward = { p1out.setPwm(0); p2out.setPwm(fv) }

  def stop = { p1out.setPwm(0); p2out.setPwm(0) }
}

object car {
  val f1 = new Leg(RaspiPin.GPIO_00, RaspiPin.GPIO_01)
  val f2 = new Leg(RaspiPin.GPIO_02, RaspiPin.GPIO_03)

  def forward = { f1.forward ; f2.forward }

  def turnLeft = { f1.forward; f2.backward }

  def turnRight = { f1.backward; f2.forward }

  def backward = { f1.backward; f2.backward }

  def stop = { f1.stop; f2.stop }
}

@main def TestGpio() = {

  car.forward

}
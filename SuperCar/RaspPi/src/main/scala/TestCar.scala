import com.pi4j.io.gpio._
import com.pi4j.io.gpio.event.{GpioPinDigitalStateChangeEvent, GpioPinListener, GpioPinListenerDigital}
import com.pi4j.io.gpio.trigger.GpioCallbackTrigger
import rs.actor.Wheel
import rs.actor.Car

import scala.io.StdIn

@main def TestCar() = {
  val car = Car()
  car.test
}
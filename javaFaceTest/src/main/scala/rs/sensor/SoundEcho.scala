package rs.sensor

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.pi4j.io.gpio.event.{GpioPinDigitalStateChangeEvent, GpioPinListener, GpioPinListenerDigital}
import com.pi4j.io.gpio.trigger.GpioCallbackTrigger
import com.pi4j.io.gpio.{GpioController, GpioFactory, GpioPinDigitalInput, PinState, RaspiPin}

import java.util.concurrent.{Callable, TimeUnit}
import rs.actor.CarCommand

case class DistanceMsg(distance: Double)

class SoundEcho(distanceHandler: ActorRef[CarCommand]) {

  val gpio: GpioController  = GpioFactory.getInstance()
  val trigger = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "", PinState.LOW)
  val echo: GpioPinDigitalInput = gpio.provisionDigitalInputPin(RaspiPin.GPIO_05, "")

  def start(): Behavior[String] = Behaviors.receive { (ctx, msg) =>

    while(true) {
      trigger.high()
      Thread.sleep(0, 10000)
      trigger.low()
      var startTime = 0L
      var endTime = 0L
      while (echo.getState == PinState.LOW) startTime = System.nanoTime()
      while (echo.getState == PinState.HIGH) endTime = System.nanoTime()
      val timeElasped = (endTime-startTime).toDouble/1000000
      val distance = timeElasped* 34.3 /2
      println(s"get distance : ${distance}")
      if (distance < 15) {
        distanceHandler.tell(CarCommand.Stop)
        Thread.sleep(10)
        distanceHandler.tell(CarCommand.Backward)
        Thread.sleep(1500)
        distanceHandler.tell(CarCommand.TurnLeft)
        Thread.sleep(1500)
        distanceHandler.tell(CarCommand.Forward)
      }
    }
    Behaviors.same
  }
}

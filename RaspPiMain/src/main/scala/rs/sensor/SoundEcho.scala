package rs.sensor

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.pi4j.io.gpio.event.{GpioPinDigitalStateChangeEvent, GpioPinListener, GpioPinListenerDigital}
import com.pi4j.io.gpio.trigger.GpioCallbackTrigger
import com.pi4j.io.gpio.{GpioController, GpioFactory, GpioPinDigitalInput, PinState, RaspiPin}
import rs.actor.CarCommand

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

object SoundEcho {
  def apply(): Behavior[Double] = {
    Behaviors.setup(context =>
      Behaviors.withTimers { timers =>
        timers.startTimerWithFixedDelay("check", 1.0, FiniteDuration(1, TimeUnit.SECONDS))

        Behaviors.same
      }
    )
  }
}
class SoundEcho(distanceHandler: ActorRef[Double]) {

  val gpio: GpioController  = GpioFactory.getInstance()
  val trigger = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, "", PinState.LOW)
  val echo: GpioPinDigitalInput = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00, "")

  def ready(): Behavior[String] = Behaviors.receive { (ctx, msg) =>
    ctx.log.debug(s"get $msg ")
    trigger.high()
    Thread.sleep(0, 10000)
    trigger.low()
    var startTime = 0L
    var endTime = 0L
    while (echo.getState == PinState.LOW) startTime = System.nanoTime()
    while (echo.getState == PinState.HIGH) endTime = System.nanoTime()
    val timeElasped = (endTime-startTime).toDouble/1000000
    val distance = timeElasped* 34.3 /2
    distanceHandler.tell(distance)

    Behaviors.same
  }
}

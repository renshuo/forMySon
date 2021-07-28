package rs.sensor

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import rs.dev.{GpioDevDigitalIn, GpioDevDigitalOut}

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

  val dev: GpioDevDigitalOut = GpioDevDigitalOut(7)
  val dev2: GpioDevDigitalIn = GpioDevDigitalIn(0)

  def ready(): Behavior[String] = Behaviors.receive { (ctx, msg) =>
    ctx.log.debug(s"get $msg ")
    dev.high
    Thread.sleep(0, 10000)
    dev.low
    var startTime = 0L
    var endTime = 0L
    while (dev2.isLow) startTime = System.nanoTime()
    while (dev2.isHigh) endTime = System.nanoTime()
    val timeElasped = (endTime-startTime).toDouble/1000000
    val distance = timeElasped* 34.3 /2
    distanceHandler.tell(distance)

    Behaviors.same
  }
}

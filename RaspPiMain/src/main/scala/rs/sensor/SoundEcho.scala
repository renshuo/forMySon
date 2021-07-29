package rs.sensor

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

import java.time.Duration
import rs.dev.{GpioDevDigitalIn, GpioDevDigitalOut}
import com.typesafe.scalalogging.Logger

object SoundEcho {

  val logger = Logger(getClass)

  def apply(distanceHandler: ActorRef[Double]): Behavior[String] = {
    Behaviors.setup(context =>
      Behaviors.withTimers { timer =>
        import scala.concurrent.duration.{FiniteDuration, SECONDS}
        timer.startTimerWithFixedDelay("check distance", FiniteDuration(1, SECONDS))
        new SoundEcho(distanceHandler).ready()
      }
    )
  }
}
class SoundEcho(distanceHandler: ActorRef[Double]) {

  val logger = Logger(getClass)

  val (dev:GpioDevDigitalOut, dev2: GpioDevDigitalIn) = {
    try{
      (GpioDevDigitalOut(7), GpioDevDigitalIn(0))
    }catch {
      case any => println(s"fail to create gpio: $any")
      (null, null)
    }
  }

  def ready(): Behavior[String] = Behaviors.receive[String] { (ctx, msg) =>
    dev.high
    Thread.sleep(0, 10000)
    dev.low
    var startTime = System.nanoTime()
    var endTime = System.nanoTime() + 1000000
    while (dev2.isLow && startTime < endTime) {
      startTime = System.nanoTime()
    }
    while (dev2.isHigh && startTime < endTime) {
      endTime = System.nanoTime()
    }
    val timeElasped = (endTime - startTime).toDouble / 1000000
    val distance = timeElasped * 34.3 / 2
    distanceHandler.tell(distance)

    Behaviors.same
  }
}

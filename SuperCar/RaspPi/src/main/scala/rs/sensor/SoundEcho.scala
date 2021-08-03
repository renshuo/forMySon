package rs.sensor

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

import java.time.Duration
import rs.dev.{GpioDevDigitalIn, GpioDevDigitalOut}
import com.typesafe.scalalogging.Logger
import rs.actor.{EchoEvent, EchoInfo}

import scala.concurrent.duration.{FiniteDuration, SECONDS}

object SoundEcho {

  val logger = Logger(getClass)

  def apply(distanceHandler: ActorRef[EchoEvent]): Behavior[String] = {
    Behaviors.setup(context =>
      Behaviors.withTimers { timer =>
        timer.startTimerWithFixedDelay("check distance", FiniteDuration(1, SECONDS))
        new SoundEcho(context, distanceHandler)
      }
    )
  }
}
class SoundEcho(ctx: ActorContext[String], distanceHandler: ActorRef[EchoEvent]) extends AbstractBehavior(ctx) {

  val logger = Logger(getClass)

  val (dev:GpioDevDigitalOut, dev2: GpioDevDigitalIn) = {
    try{
      (GpioDevDigitalOut(7), GpioDevDigitalIn(0))
    }catch {
      case any => println(s"fail to create gpio: $any")
      (null, null)
    }
  }

  override def onMessage(msg: String): Behavior[String] = {
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
    distanceHandler.tell(EchoInfo(distance, 90.0d))

    Behaviors.same
  }

}

package rs.sensor

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import rs.dev.{GpioDevDigitalIn, GpioDevDigitalOut}

import scala.concurrent.Future
import org.slf4j.{Logger, LoggerFactory}

object SoundEcho {

  val log: Logger = LoggerFactory.getLogger(getClass)

  def apply(distanceHandler: ActorRef[Double]): Behavior[String] = {
    log.debug("create sound echo.")
    Behaviors.setup(context =>
      Behaviors.withTimers { timers =>
        //timers.startTimerWithFixedDelay("ss", FiniteDuration(1, TimeUnit.SECONDS))
        new SoundEcho(distanceHandler).ready()
      }
    )
  }
}
class SoundEcho(distanceHandler: ActorRef[Double]) {

  val log: Logger = LoggerFactory.getLogger(getClass)

  log.debug("init sound echo class")

  val (dev:GpioDevDigitalOut, dev2: GpioDevDigitalIn) = {
    try{
      (GpioDevDigitalOut(7), GpioDevDigitalIn(0))
    }catch {
      case any => println(s"fail to create gpio: $any")
      (null, null)
    }
  }


  def ready(): Behavior[String] = Behaviors.receive { (ctx, msg) =>
    Future {
      ctx.log.info(s"check distance.")
      dev.high
      Thread.sleep(0, 10000)
      dev.low
      var startTime = System.nanoTime()
      var endTime = System.nanoTime() + 1000000
      while (dev2.isLow && startTime<endTime) {
        startTime = System.nanoTime()
      }
      while (dev2.isHigh && startTime<endTime) {
        endTime = System.nanoTime()
      }
      val timeElasped = (endTime - startTime).toDouble / 1000000
      val distance = timeElasped * 34.3 / 2
      distanceHandler.tell(distance)
    }(ctx.executionContext)
    Behaviors.same
  }
}

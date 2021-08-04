package rs.sensor

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

import java.time.Duration
import rs.dev.{GpioDevDigitalIn, GpioDevDigitalOut}
import com.typesafe.scalalogging.Logger
import rs.actor.{BaseCommand, EchoDirection, EchoEvent, EchoInfo}

import scala.concurrent.duration.{FiniteDuration, SECONDS}

object SoundEcho {

  val logger = Logger(getClass)


  import rs.controllerKey
  var controllerList = Set[ActorRef[BaseCommand]]()

  def apply(): Behavior[EchoEvent] = {
    Behaviors.setup( (context:ActorContext[EchoEvent]) =>

      val blist = context.spawn(Behaviors.receiveMessage{ (list: Receptionist.Listing) =>
        val si: Set[ActorRef[BaseCommand]] = list.serviceInstances(controllerKey)
        this.controllerList = si
        println(s"update actor A list: ${this.controllerList}")
        Behaviors.same
      }, "ctlList")
      context.system.receptionist ! Receptionist.Subscribe(controllerKey, blist.ref)

      Behaviors.withTimers { timer =>
        timer.startTimerWithFixedDelay(EchoInfo(0,0), FiniteDuration(1, SECONDS))
        new SoundEcho(context)
      }
    )
  }
}
class SoundEcho(ctx: ActorContext[EchoEvent]) extends AbstractBehavior(ctx) {
  import SoundEcho.controllerList

  val logger = Logger(getClass)

  val (dev:GpioDevDigitalOut, dev2: GpioDevDigitalIn) = {
    try{
      (GpioDevDigitalOut(7), GpioDevDigitalIn(0))
    }catch {
      case any => println(s"fail to create gpio: $any")
      (null, null)
    }
  }

  override def onMessage(msg: EchoEvent): Behavior[EchoEvent] = {
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
    controllerList.foreach { (ctl: ActorRef[BaseCommand]) =>
      ctl.tell(EchoInfo(distance, 90.0d))
    }
    Behaviors.same
  }

}

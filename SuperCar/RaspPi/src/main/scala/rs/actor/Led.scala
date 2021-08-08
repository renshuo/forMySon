package rs.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import rs.dev.{GpioDev, GpioDevDigitalOut}

import scala.concurrent.{ExecutionContext, Future}

object Led{

  def apply() = Behaviors.setup[LedCommand] { ctx =>
    new Led(ctx)
  }
}

class Led(ctx: ActorContext[LedCommand]) extends AbstractBehavior[LedCommand](ctx){

  var ledDev:GpioDevDigitalOut = _

  override def onMessage(msg: LedCommand): Behavior[LedCommand] = {
    msg match {
      case ledInit: LedInit => {
        println("init LED.")
        ledDev = new GpioDevDigitalOut(25)
        ledDev.high
      }
      case ledToggle: LedToggle => {
        ledDev.toggle
      }
      case LedBlink(freq, times) => {
        given ec: ExecutionContext = ctx.executionContext
        Future {
          for (i <- 0 until times) {
            ledDev.high
            Thread.sleep((Math.ceil(1000 / freq * 0.4)).toLong)
            ledDev.low
            Thread.sleep((Math.ceil(1000 / freq * 0.6)).toLong)
          }
        }
      }
    }
    Behaviors.same
  }
}

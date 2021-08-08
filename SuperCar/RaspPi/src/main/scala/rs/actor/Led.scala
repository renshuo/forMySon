package rs.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import rs.dev.{GpioDev, GpioDevDigitalOut}

object Led{

  def apply() = Behaviors.setup[LedCommand] { ctx =>
    new Led(ctx)
  }
}

class Led(ctx: ActorContext[LedCommand]) extends AbstractBehavior[LedCommand](ctx){

  var ledDev:GpioDevDigitalOut = _

  override def onMessage(msg: LedCommand): Behavior[LedCommand] = Behaviors.receiveMessage { (msg: LedCommand) =>
    msg match {
      case ledInit: LedInit => {
        ledDev = new GpioDevDigitalOut(25)
      }
      case ledToggle: LedToggle => {
        ledDev.toggle
      }
    }
    Behaviors.same
  }
}

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

  var ledStatus = true


  val led = new GpioDevDigitalOut(25)


  override def onMessage(msg: LedCommand): Behavior[LedCommand] = {
    ledStatus = !ledStatus
    if (ledStatus) led.high else led.low
    Behaviors.same
  }
}

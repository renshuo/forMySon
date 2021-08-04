package rs.controller

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, DispatcherSelector}
import rs.actor.*
import rs.sensor.*
import org.slf4j.{Logger, LoggerFactory}

object EchoController {

  val log: Logger = LoggerFactory.getLogger(getClass)

  def apply(car: ActorRef[CarCommand]): Behavior[String]= {
    log.info("create Echo controller")
    Behaviors.setup[String](ctx => new EchoController(ctx, car))
  }
}

class EchoController(context: ActorContext[String], car: ActorRef[CarCommand]) extends AbstractBehavior[String](context) {
  

  val handler = context.spawn( Behaviors.receive[EchoEvent] { (ctx, distance) =>
    ctx.log.debug(s"get distance : ${distance}")
    if (distance.asInstanceOf[EchoInfo].distance < 15) {
      car.tell(Stop())
      Thread.sleep(200)
      car.tell(Backward(40))
      Thread.sleep(1500)
      car.tell(Stop())
      Thread.sleep(200)
      car.tell(TurnLeft(60))
      Thread.sleep(1500)
      car.tell(Stop())
      Thread.sleep(200)
      car.tell(Forward(40))
    }
    Behaviors.same
  }, "handler")

  val echo: ActorRef[EchoEvent] = context.spawn(SoundEcho(), "echo" )

  override def onMessage(msg: String): Behavior[String] = {
    context.log.info("start echo controller.")
    Behaviors.same
  }
}

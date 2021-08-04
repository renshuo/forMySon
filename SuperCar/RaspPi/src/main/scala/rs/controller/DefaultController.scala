package rs.controller

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import com.typesafe.scalalogging.Logger
import rs.actor.{BaseCommand, CarCommand, EchoDirection, EchoInfo, TripodCommand}
import rs.sensor.SoundEcho



object DefaultController {

  import rs.controllerKey

  def apply(car: ActorRef[CarCommand], tripod: ActorRef[TripodCommand], echo: ActorRef[EchoDirection]): Behavior[BaseCommand] = {
    Behaviors.setup{ ctx =>
      ctx.system.receptionist ! Receptionist.Register(controllerKey, ctx.self)
      new DefaultController(ctx, car, tripod, echo)
    }
  }

}

class DefaultController(ctx: ActorContext[BaseCommand], car: ActorRef[CarCommand], tripod: ActorRef[TripodCommand], echo: ActorRef[EchoDirection]) extends AbstractBehavior[BaseCommand](ctx) {

  val log = Logger(getClass)

  override def onMessage(msg: BaseCommand): Behavior[BaseCommand] = {
    // how to deal with a CarCommand or TripodCommand
    msg match {
      case carCommand: CarCommand => {
        log.info(s"get a car command: ${carCommand}")
        car.tell(carCommand)
      }
      case tripodCommand: TripodCommand => {
        log.info(s"get a tripod command: ${tripodCommand}")
        tripod.tell(tripodCommand)
      }
      case echoInfo: EchoInfo => {
        log.info(s"get a echo event: ${echoInfo}")
      }
    }
    Behaviors.same
  }
}

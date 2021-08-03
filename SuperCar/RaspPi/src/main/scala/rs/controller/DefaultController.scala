package rs.controller

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.typesafe.scalalogging.Logger
import rs.actor.{BaseCommand, CarCommand, TripodCommand}


object DefaultController {

  def apply(car: ActorRef[CarCommand], tripod: ActorRef[TripodCommand]): Behavior[BaseCommand] = {
    Behaviors.setup( ctx => new DefaultController(ctx, car, tripod))
  }
}

class DefaultController(ctx: ActorContext[BaseCommand], car: ActorRef[CarCommand], tripod: ActorRef[TripodCommand]) extends AbstractBehavior[BaseCommand](ctx) {

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
    }
    Behaviors.same
  }
}

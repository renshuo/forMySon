package rs.controller

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import com.typesafe.scalalogging.Logger
import rs.actor.*
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

  val ledDev = ctx.spawn(Led(), "led")
   ledDev.tell(LedInit())

  val joy: ActorRef[ActorRef[JoyCommand]] = ctx.spawn(JoySticker(), "joy")
  joy.tell(ctx.self)

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
      case ledCommand: LedCommand => {
        ledDev.tell(ledCommand)
      }
      case JoyBtnEvent(btnNum, isDown) => handleJoyBtnEvent(btnNum, isDown)
      case JoyAxisEvent(axisNum, axisValue) => handleJoyAxisEvent(axisNum, axisValue)
    }
    Behaviors.same
  }

  private def handleJoyAxisEvent(axisNum: Int, axisValue: Double) = {

    println(s"get btn ${axisNum} ${axisValue}")
    car ! (axisNum match {
      case 1 => {
        axisValue match {
          case x if x == 0 => Stop()
          case x if x > 0 => Backward(50)
          case x if x < 0 => Forward(50)
        }
      }
      case 0 => {
        axisValue match {
          case x if x == 0 => Stop()
          case x if x > 0 => TurnRight(50)
          case x if x < 0 => TurnLeft(50)
        }
      }
      case _ => Stop()
    })
  }

  private def handleJoyBtnEvent(btnNum: Int, isDown: Boolean) = {
    println(s"get btn ${btnNum}")
    val tripodBaseDegree = 3
    val tripodUpdateDelay = 20
    btnNum match {
      case 3 => tripod ! (if isDown then TripodVelocity(2, 0) else TripodVelocity(0, 0))
      case 1 => tripod ! (if isDown then TripodVelocity(-2, 0) else TripodVelocity(0, 0))
      case 0 => tripod ! (if isDown then TripodVelocity(0, -2) else TripodVelocity(0, 0))
      case 2 => tripod ! (if isDown then TripodVelocity(0, 2) else TripodVelocity(0, 0))
      case 4 => car ! (if isDown then MoveRight(70) else Stop())
      case 5 => car ! (if isDown then MoveLeft(70) else Stop())
      case 9 => ledDev ! LedToggle()
      case 8 => ledDev ! LedToggle()
      case _ => {}
    }
  }
}

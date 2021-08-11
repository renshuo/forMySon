package rs.controller

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import com.typesafe.scalalogging.Logger
import rs.actor.*
import rs.sensor.SoundEcho

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration



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
  ctx.scheduleOnce(FiniteDuration(3, TimeUnit.SECONDS), ledDev, LedInit())

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
    axisNum match {
      case 7 => {
        car ! (axisValue match {
          case x if x == 0 => Stop()
          case x if x > 0 => Backward(50)
          case x if x < 0 => Forward(50)
          case _ => Stop()
        })
      }
      case 6 => {
        car ! (axisValue match {
          case x if x == 0 => Stop()
          case x if x > 0 => TurnRight(50)
          case x if x < 0 => TurnLeft(50)
          case _ => Stop()
        })
      }
      case _ => {}
    }
  }

  private def handleJoyBtnEvent(btnNum: Int, isDown: Boolean) = {
    val tripodBaseDegree = 3
    val tripodUpdateDelay = 20
    btnNum match {
      case 2 => tripod ! (if isDown then TripodVelocity(2, 0) else TripodVelocity(0, 0))
      case 1 => tripod ! (if isDown then TripodVelocity(-2, 0) else TripodVelocity(0, 0))
      case 0 => tripod ! (if isDown then TripodVelocity(0, 2) else TripodVelocity(0, 0))
      case 3 => tripod ! (if isDown then TripodVelocity(0, -2) else TripodVelocity(0, 0))
      case 4 => car ! (if isDown then MoveRight(70) else Stop())
      case 5 => car ! (if isDown then MoveLeft(70) else Stop())
      case 6 => if isDown then ledDev ! LedToggle()
      case 7 => if isDown then ledDev ! LedBlink(3, 6)
      case _ => {}
    }
  }
}

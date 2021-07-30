package rs.controller

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import org.bytedeco.javacv.Frame
import org.bytedeco.opencv.opencv_core.Mat

import rs.actor.*
import rs.detector.*

object MainController {
  def apply(): Behavior[String]= {
    Behaviors.setup[String](ctx => new MainController(ctx))
  }
}

class MainController(ctx: ActorContext[String]) extends AbstractBehavior[String](ctx) {

  val vshow: ActorRef[Frame] = ctx.spawn(VideoShow().show(), "vshow")
  val faceDect: ActorRef[Mat] = ctx.spawn(FaceDnnActor(vshow).detect(), "detector")
  val grabber = ctx.spawn(VideoGrabber(faceDect).grab(), "grabber")

  val joy: ActorRef[ActorRef[JoyCommand]] = ctx.spawn(JoySticker(), "joy")

  val webActor: ActorRef[CarCommand | TripodCommand] = ctx.spawn(WebClientActor(), "webClient")

  val joyEventHandler = ctx.spawn(Behaviors.receive[JoyCommand]{ (ctx, ev:JoyCommand) =>
    ev match {
      case JoyBtnEvent(btnNum, isDown) => {
        println(s"get btn ${btnNum}")
        btnNum match {
          case 4 => webActor ! TripodUpdate(1, 0)
          case 5 => webActor ! TripodUpdate(-1, 0)
          case 6 => webActor ! TripodUpdate(0, 1)
          case 7 => webActor ! TripodUpdate(0, -1)
          case _ => {}
        }
      }
      case JoyAxisEvent(axisNum, axisValue) => {
        println(s"get btn ${axisNum} ${axisValue}")
        webActor ! (axisNum match {
          case 1 => {
            axisValue match {
              case x if x==0 => Stop()
              case x if x>0 => Backward(50)
              case x if x<0 => Forward(50)
            }
          }
          case 0 => {
            axisValue match {
              case x if x==0 => Stop()
              case x if x>0 => TurnRight(50)
              case x if x<0 => TurnLeft(50)
            }
          }
          case _ => Stop()
        })
      }
    }
    Behaviors.same
  }, "joyHandler")

  override def onMessage(msg: String): Behavior[String] = {
    ctx.log.info(s"$msg monitor")
    grabber.tell("http://192.168.31.242:8081")
    joy.tell(joyEventHandler)
    Behaviors.empty
  }
}

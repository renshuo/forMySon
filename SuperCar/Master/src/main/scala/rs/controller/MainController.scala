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

  val webActor: ActorRef[String] = ctx.spawn(WebClientActor(), "webClient")

  val joyEventHandler = ctx.spawn(Behaviors.receive[JoyCommand]{ (ctx, ev:JoyCommand) =>
    ev match {
      case x: JoyBtnEvent => println(s"get btn ${x}")
      case y: JoyAxisEvent => println(s"get btn ${y}")
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

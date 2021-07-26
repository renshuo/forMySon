
import akka.actor.typed.{ActorSystem, ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, AbstractBehavior, Behaviors}

import org.bytedeco.javacpp.indexer.FloatIndexer
import org.bytedeco.javacv.{Frame}
import org.bytedeco.opencv.opencv_core.{Mat}

import rs.actor.{VideoGrabber, VideoShow}
import rs.detector.{FaceDnnActor}


@main def akkaMain() = {
  val sys = ActorSystem(MonitorAct(), "camera")
  sys.tell("start")
}

object MonitorAct {
  def apply(): Behavior[String]= {
    Behaviors.setup[String](ctx => new MonitorAct(ctx))
  }
}

class MonitorAct(ctx: ActorContext[String]) extends AbstractBehavior[String](ctx) {

  val vshow: ActorRef[Frame] = ctx.spawn(VideoShow().show(), "vshow")
  val faceDect: ActorRef[Mat] = ctx.spawn(FaceDnnActor(vshow).detect(), "detector")
  val grabber = ctx.spawn(VideoGrabber(faceDect).grab(), "grabber")

  override def onMessage(msg: String): Behavior[String] = {
    ctx.log.info(s"$msg monitor")
    grabber.tell("http://192.168.0.242:8081")
    Behaviors.empty
  }
}



import akka.actor.typed._
import akka.actor.typed.scaladsl._
import akka.cluster.ClusterEvent._
import akka.cluster.MemberStatus
import akka.cluster.typed._

import org.bytedeco.javacpp.indexer.FloatIndexer
import org.bytedeco.javacv._
import org.bytedeco.opencv.opencv_core._
import org.bytedeco.opencv.opencv_dnn._
import org.bytedeco.opencv.opencv_imgproc._
import org.bytedeco.opencv.opencv_videoio._
import org.bytedeco.opencv.global.opencv_core._
import org.bytedeco.opencv.global.opencv_dnn._
import org.bytedeco.opencv.global.opencv_imgproc._
import org.bytedeco.opencv.global.opencv_videoio._


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


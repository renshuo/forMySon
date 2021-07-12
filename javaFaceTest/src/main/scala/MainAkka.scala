
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
//  val cluster = Cluster(sys)
//  cluster.manager.tell(cluster.selfMember.address)
}

object MonitorAct {
  def apply(): Behavior[String]= {
    Behaviors.setup[String](ctx => new MonitorAct(ctx))
  }
}

class MonitorAct(ctx: ActorContext[String]) extends AbstractBehavior[String](ctx) {

  val frameAct = ctx.spawn(FrameAct(), "frame")
  val camera = ctx.spawn(CameraAct(frameAct.ref), "camera")

  override def onMessage(msg: String): Behavior[String] = {
    ctx.log.info(s"$msg monitor")
    camera.tell(CameraCommand.Start)
    Behaviors.empty
  }
}
import akka.actor.TypedActor
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors

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

enum CameraCommand:
  case Start, Stop

object CameraAct {
  def apply(frameAct: ActorRef[Mat]): Behavior[CameraCommand]= {
    println("init CameraAct")
    Behaviors.setup[CameraCommand](ctx => new CameraAct(ctx, frameAct))
  }
}

class CameraAct(ctx: ActorContext[CameraCommand], freamAct: ActorRef[Mat]) extends AbstractBehavior[CameraCommand](ctx) {

  override def onMessage(msg: CameraCommand): Behavior[CameraCommand] = {
    ctx.log.info(s"Camera get a msg: $msg")
    freamAct.tell(new Mat())
    ctx.log.info("sended mat image to frame processor")
    Behaviors.empty
  }

}

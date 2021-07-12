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

object FrameAct {
  def apply(): Behavior[Mat]= {
    println("init FrameAct")
    Behaviors.setup[Mat](ctx => new FrameAct(ctx))
  }
}

class FrameAct(ctx: ActorContext[Mat]) extends AbstractBehavior[Mat](ctx) {

  override def onMessage(msg: Mat): Behavior[Mat] = {
    ctx.log.info(s"get a frame: $msg")
    Behaviors.empty
  }

}

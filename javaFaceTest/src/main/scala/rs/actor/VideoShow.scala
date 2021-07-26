package rs.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import org.bytedeco.javacv.{CanvasFrame, Frame}
import org.bytedeco.opencv.opencv_core.Mat

class VideoShow {

  val mainframe: CanvasFrame = new CanvasFrame("Face Detection", CanvasFrame.getDefaultGamma / 2.2)
  mainframe.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE)
  mainframe.setCanvasSize(640, 480)
  mainframe.setVisible(true)

  def show(): Behavior[Frame] = Behaviors.receive { (ctx, msg: Frame) =>
    mainframe.showImage(msg)
    Behaviors.same
  }
}


import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.CanvasFrame

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

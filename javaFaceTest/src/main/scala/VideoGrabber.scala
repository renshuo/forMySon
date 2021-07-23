import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import org.bytedeco.javacv.{FFmpegFrameGrabber, FrameGrabber}
import org.bytedeco.javacv._
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.javacv.OpenCVFrameConverter
import org.bytedeco.ffmpeg.global.avutil

class VideoGrabber(detector: ActorRef[Mat]) {

  def grab(): Behavior[String] =
    Behaviors.receive { (ctx, url) =>
      // val url = "http://192.168.0.242:8081"
      ctx.log.info(s"start grab from ${url}")
      avutil.av_log_set_level(avutil.AV_LOG_ERROR)

      val gra = FFmpegFrameGrabber.createDefault(url)
      gra.start()
      val converter: OpenCVFrameConverter.ToMat = new OpenCVFrameConverter.ToMat
      Thread.sleep(1000)
      while(true) {
        val img: Mat = converter.convert(gra.grab())
        detector.tell(img.clone())
      }
      Behaviors.same
    }

}

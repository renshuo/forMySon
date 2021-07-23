import akka.actor.typed.Behavior
import akka.actor.typed.ActorRef
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
import org.bytedeco.ffmpeg.global.avutil
import org.bytedeco.javacv.{FFmpegFrameGrabber, FrameGrabber}


class FaceDnnActor(vshow: ActorRef[Frame]) {

  val net: Net = readNetFromCaffe("./deploy.prototxt", "./res10_300x300_ssd_iter_140000_fp16.caffemodel")
  val converter: OpenCVFrameConverter.ToMat = new OpenCVFrameConverter.ToMat

  def detect(): Behavior[Mat] = Behaviors.receive { (ctx, img:Mat) =>
    // ctx.log.info(s"get grabbed img: ${img}")
    resize(img, img, new Size(300, 300));
    //val blob = blobFromImage(colorimg)
    val blob = blobFromImage(img, 1.0, new Size(300, 300), new Scalar(104.0, 177.0, 123.0, 0), false, false, CV_32F)
    net.setInput(blob)
    val output = net.forward()

    val ne = new Mat(new Size(output.size(3), output.size(2)), CV_32F, output.ptr(0, 0)); //extract a 2d matrix for 4d output matrix with form of (number of detections x 7)
    val srcIndexer: FloatIndexer = ne.createIndexer

    for (i <- 0 until output.size(3)) {
      val confidence: Float = srcIndexer.get(i, 2)
      val f1: Float = srcIndexer.get(i, 3)
      val f2: Float = srcIndexer.get(i, 4)
      val f3: Float = srcIndexer.get(i, 5)
      val f4: Float = srcIndexer.get(i, 6)
      if (confidence > 0.6) {
        val tx: Float = f1 * 300; //top left point's x
        val ty: Float = f2 * 300; //top left point's y
        val bx: Float = f3 * 300; //bottom right point's x
        val by: Float = f4 * 300; //bottom right point's y
        //println(s"get face at: ${tx}, $ty, $bx, $by")
        rectangle(img, new Rect(new Point(tx.toInt, ty.toInt), new Point(bx.toInt, by.toInt)), new Scalar(255, 0, 0, 0)) //print blue rectangle
        //ctx.log.info(s"draw rectangle: ${img}")
      }
    }
    val newimg = converter.convert(img)
    vshow.tell(newimg)
    Behaviors.same
  }
}

@main
def FaceDnn(cameraDev: String): Unit ={
  // input
  println("start")
  avutil.av_log_set_level(avutil.AV_LOG_ERROR)
  //val url = "http://192.168.1.100:8081"
  val url = "http://192.168.0.242:8081"
  val gra = FFmpegFrameGrabber.createDefault(url)
  gra.start()
  println(s"catched input ")

  //process
  val net: Net = readNetFromCaffe("./deploy.prototxt", "./res10_300x300_ssd_iter_140000_fp16.caffemodel")
  var colorimg:Mat = new Mat()
  import org.bytedeco.javacv.OpenCVFrameConverter
  val converter: OpenCVFrameConverter.ToMat = new OpenCVFrameConverter.ToMat
  println("created process object")

  //output
  val mainframe: CanvasFrame = new CanvasFrame("Face Detection", CanvasFrame.getDefaultGamma / 2.2)
  mainframe.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE)
  mainframe.setCanvasSize(640, 480)
  mainframe.setVisible(true)

  //Thread.sleep(1000)
  println("before check")
  var count = 0
  while(mainframe.isVisible) {
    count += 1
    colorimg = converter.convert(gra.grab())

    if (count >0) {
      resize(colorimg, colorimg, new Size(300, 300));
      //val blob = blobFromImage(colorimg)
      val blob = blobFromImage(colorimg, 1.0, new Size(300, 300), new Scalar(104.0, 177.0, 123.0, 0), false, false, CV_32F)
      net.setInput(blob)
      val output = net.forward()

      val ne = new Mat(new Size(output.size(3), output.size(2)), CV_32F, output.ptr(0, 0));//extract a 2d matrix for 4d output matrix with form of (number of detections x 7)
      val srcIndexer: FloatIndexer = ne.createIndexer

      for (i <- 0 until output.size(3)) {
        val confidence: Float = srcIndexer.get(i, 2)
        val f1: Float = srcIndexer.get(i, 3)
        val f2: Float = srcIndexer.get(i, 4)
        val f3: Float = srcIndexer.get(i, 5)
        val f4: Float = srcIndexer.get(i, 6)
        if (confidence > 0.6) {
          val tx: Float = f1 * 300; //top left point's x
          val ty: Float = f2 * 300; //top left point's y
          val bx: Float = f3 * 300; //bottom right point's x
          val by: Float = f4 * 300; //bottom right point's y
          //println(s"get face at: ${tx}, $ty, $bx, $by")
          rectangle(colorimg, new Rect(new Point(tx.toInt, ty.toInt), new Point(bx.toInt, by.toInt)), new Scalar(255, 0, 0, 0)) //print blue rectangle
        }

      }
    }

    mainframe.showImage(converter.convert(colorimg))
  }
}
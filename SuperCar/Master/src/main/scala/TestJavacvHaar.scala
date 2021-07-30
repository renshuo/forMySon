
import java.io.File
import java.net.URL
import org.bytedeco.javacv._
import org.bytedeco.javacpp._
import org.bytedeco.javacpp.indexer._
import org.bytedeco.opencv.opencv_core._
import org.bytedeco.opencv.opencv_imgproc._
import org.bytedeco.opencv.opencv_calib3d._
import org.bytedeco.opencv.opencv_objdetect._
import org.bytedeco.opencv.global.opencv_core._
import org.bytedeco.opencv.global.opencv_imgproc._
import org.bytedeco.opencv.global.opencv_calib3d._
import org.bytedeco.opencv.global.opencv_objdetect._

import org.opencv.imgproc.Imgproc

@main
def TestJavacvHaar() = {

  //System.setProperty("java.library.path", "/home/work/project/forMySon/javaFaceTest/.idea/libraries")
  // System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
  val converter: OpenCVFrameConverter.ToMat = new OpenCVFrameConverter.ToMat()

  // input
  val gra = FrameGrabber.createDefault(0) //FFmpegFrameGrabber("/home/work/test.mp4")
  gra.start()
  val (height, width) = {
    var grabbedImage: Mat = converter.convert(gra.grab())
    (grabbedImage.rows(), grabbedImage.cols())
  }
  // process

  val grayImage: Mat = new Mat(height, width, CV_8UC1)
  val haar = new CascadeClassifier("haarcascade_frontalface_default.xml")

  //output
  val frame: CanvasFrame = new CanvasFrame("Some Title", CanvasFrame.getDefaultGamma / gra.getGamma)

  while (frame.isVisible) {
    val grabbedImage = converter.convert(gra.grab())
    cvtColor(grabbedImage, grayImage, CV_BGR2GRAY)
    val faces: RectVector = new RectVector
    haar.detectMultiScale(grayImage, faces)
    val total: Int = faces.size.toInt
    for (i <- 0 until total) {
      val r: Rect = faces.get(i)
      val (x,y,w,h) = (r.x, r.y, r.width, r.height)
      rectangle(grabbedImage, new Point(x, y), new Point(x + w, y + h), AbstractScalar.RED, 1, CV_AA, 0)
    }
    frame.showImage(converter.convert(grabbedImage))
  }
}
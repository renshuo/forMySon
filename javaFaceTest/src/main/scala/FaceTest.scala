
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
def FaceTest() = {

  //System.setProperty("java.library.path", "/home/work/project/forMySon/javaFaceTest/.idea/libraries")
  println(System.getProperty("java.library.path"))
  // System.loadLibrary(Core.NATIVE_LIBRARY_NAME)


  println("hello world")
  val gra = FFmpegFrameGrabber("/home/work/test.mp4")
  gra.start()
  val len = gra.getLengthInAudioFrames()
  println(s"video len $len")

  import org.bytedeco.javacv.OpenCVFrameConverter
  val converter: OpenCVFrameConverter.ToMat = new OpenCVFrameConverter.ToMat()
  val haar = new CascadeClassifier("haarcascade_frontalface_default.xml")

  val recorder: FrameRecorder = FrameRecorder.createDefault("output.avi", 640, 480)
  recorder.start()

  for ( i <- 1 to len) {
    val frame = gra.grabFrame()
    val grabbedImage:Mat = converter.convert(frame)

    if (frame != null && frame.image != null) {

        val faces = new RectVector
        haar.detectMultiScale(grabbedImage, faces) //.detectMultiScale(img, faces)
        println(faces.size())
        if (faces.size()==1) {
          for(i <- 0 until faces.size().toInt ) {
            val r: Rect = faces.get(i)
            val x: Int = r.x
            val y: Int = r.y
            val w: Int = r.width
            val h: Int = r.height
            rectangle(grabbedImage, new Point(x, y), new Point(x + w, y + h), new Scalar(0.0, 0.0, 255.0, 0.0), 1, CV_AA, 0)
            println(faces.get(i))
          }


          import org.opencv.imgcodecs.Imgcodecs
          recorder.record(converter.convert(grabbedImage))
        }

    }
  }
}
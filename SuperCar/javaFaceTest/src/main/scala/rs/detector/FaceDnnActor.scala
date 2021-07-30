package rs.detector

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import org.bytedeco.javacpp.indexer.FloatIndexer
import org.bytedeco.javacv.{Frame, OpenCVFrameConverter}
import org.bytedeco.opencv.global.opencv_core.CV_32F
import org.bytedeco.opencv.global.opencv_dnn.{blobFromImage, readNetFromCaffe}
import org.bytedeco.opencv.global.opencv_imgproc.{rectangle, resize}
import org.bytedeco.opencv.opencv_core._
import org.bytedeco.opencv.opencv_dnn.Net


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

import ai.djl.engine.Engine
import ai.djl.inference.Predictor
import ai.djl.modality.cv.output.{BoundingBox, DetectedObjects}
import ai.djl.modality.cv.util.NDImageUtils
import ai.djl.modality.cv.{Image, ImageFactory}
import ai.djl.modality.{Classifications, cv}
import ai.djl.ndarray.{NDArray, NDList, NDManager}
import ai.djl.repository.zoo.{Criteria, ZooModel}
import ai.djl.training.util.ProgressBar
import ai.djl.translate.{Batchifier, TranslateException, Translator, TranslatorContext}
import ai.djl.{Application, Model, ModelException}
import org.slf4j.{Logger, LoggerFactory}

import java.io.IOException
import java.nio.file._
import java.util
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.{Collectors, IntStream}

@main
def TestDjl(): Unit = {
  val facePath = Paths.get("./largest_selfie.jpg")
  val img = ImageFactory.getInstance().fromFile(facePath)

  val confThresh = 0.85f
  val nmsThresh = 0.45f
  val variance = Array(0.1D, 0.2D)
  val topK = 5000
  val scales = Array(Array(16, 32), Array(64, 128), Array(256, 512))
  val steps = Array(8, 16, 32)

  val criteria = Criteria.builder
    .setTypes(classOf[Image], classOf[DetectedObjects])
    .optModelPath(Path.of("./retinaface.zip"))
    .optModelName("retinaface")
    .optTranslator(new FaceDetectionTranslator(confThresh, nmsThresh, variance, topK, scales, steps))
    .optProgress(new ProgressBar())
    .optEngine("PyTorch")
    .build()

  val model = criteria.loadModel()
  val predictor = model.newPredictor()

  val detection = predictor.predict(img)

  val result = img.duplicate(Image.Type.TYPE_INT_ARGB)
  result.drawBoundingBoxes(detection)

  result.save(Files.newOutputStream(Path.of("./test.png")), "png")
  for (i <- 0 until detection.getNumberOfObjects) {
    val face: DetectedObjects.DetectedObject = detection.item(i)
    println(s"get detection: $face")
  }
}


class FcTrans extends Translator[Image, DetectedObjects] {
  override def getBatchifier: Batchifier = Batchifier.STACK

  override def processInput(ctx: TranslatorContext, input: Image): NDList = {
    val (w, h) = (input.getWidth, input.getHeight)
    new NDList()
  }

  override def processOutput(ctx: TranslatorContext, list: NDList): DetectedObjects = {

    val retNames: util.List[String] = new util.ArrayList[String]
    val retProbs: util.List[java.lang.Double] = new util.ArrayList[java.lang.Double]
    val retBB: util.List[BoundingBox] = new util.ArrayList[BoundingBox]

    return new DetectedObjects(retNames, retProbs, retBB)
  }


}
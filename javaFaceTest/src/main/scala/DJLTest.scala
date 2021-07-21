
import ai.djl.ModelException
import ai.djl.engine.Engine
import ai.djl.inference.Predictor
import ai.djl.modality.cv.Image
import ai.djl.modality.cv.ImageFactory
import ai.djl.modality.cv.output.DetectedObjects
import ai.djl.repository.zoo.Criteria
import ai.djl.repository.zoo.ZooModel
import ai.djl.training.util.ProgressBar
import ai.djl.translate.TranslateException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ai.djl.modality.cv.output.DetectedObjects
import ai.djl.repository.zoo.Criteria
import ai.djl.training.util.ProgressBar

import java.awt.Image


@main
def DJLTest(): Unit = {
  println(Engine.getInstance().getEngineName)
  val facePath = Paths.get("src/test/resources/largest_selfie.jpg")

  val confThresh = 0.85f
  val nmsThresh = 0.45f
  val variance = Array(0.1f, 0.2f)
  val topK = 5000
  val scales = Array(Array(16, 32), Array(64, 128), Array(256, 512))
  val steps = Array(8, 16, 32)

  val criteria = Criteria.builder
    .setTypes(classOf[Nothing], classOf[DetectedObjects])
    .optModelPath(Path.of("/home/work/ulrtanet.zip"))
    //.optModelUrls("https://resources.djl.ai/test-models/pytorch/ultranet.zip")
    //.optTranslator(translator)
    .optProgress(new ProgressBar)
    //.optEngine("")
    .build // Use PyTorch engine


  criteria.loadModel()


}
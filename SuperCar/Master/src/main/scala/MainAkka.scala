
import akka.actor.typed.{ActorSystem, ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, AbstractBehavior, Behaviors}

import org.bytedeco.javacpp.indexer.FloatIndexer
import org.bytedeco.javacv.{Frame}
import org.bytedeco.opencv.opencv_core.{Mat}

import rs.controller._

@main def MainAkka() = {
  val sys = ActorSystem(MainController(), "camera")
  sys.tell("start")
}




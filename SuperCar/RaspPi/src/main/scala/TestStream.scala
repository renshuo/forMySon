import akka.actor.typed.{ActorSystem, Behavior, Dispatchers}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.stream.*
import akka.stream.scaladsl.*
import akka.{Done, NotUsed}
import akka.util.ByteString

import scala.concurrent.*
import scala.concurrent.duration.*
import java.nio.file.Paths
import scala.util.{Failure, Success}

@main def TestStream = {
  val system = ActorSystem.create( TestStreamAct(), "system")
  system.tell("start")
  given sys: ActorSystem[String] = system

  val source = Source(1 to 99)

  val done: Future[Done] = source.runForeach(x => println(x))

  given x: ExecutionContext = system.executionContext
  done.onComplete {
    case Success(value) => println(s"success : ${value}")
    case Failure(exception) => println(s"error: ${exception}")
  }

  val done2 = source.runForeach( x=> println(-x))

  Source("abc".getBytes).runWith(Sink.foreach( x => println(x)))
}


object TestStreamAct {
  def apply(): Behavior[String] = {
    Behaviors.setup(ctx => new TestStreamAct(ctx))
  }
}

class TestStreamAct(ctx: ActorContext[String]) extends AbstractBehavior[String](ctx) {

  override def onMessage(msg: String): Behavior[String] = {
    println(s"get msg: ${msg}")
    Behaviors.same
  }
}
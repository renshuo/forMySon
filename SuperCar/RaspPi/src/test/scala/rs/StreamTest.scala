package rs

import akka.NotUsed
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.{BoundedSourceQueue, Materializer}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import org.reactivestreams.Subscriber
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class StreamTest extends AnyFlatSpec with Matchers{

  "base test " should "test stream base" in {
    val system = ActorSystem(Behaviors.empty, "test")
    given ex: ExecutionContext = system.executionContext
    given ma: Materializer = Materializer(system)

    val source: Source[Int, NotUsed] = Source(1 to 100)

    val flow1: Flow[Int, Int, NotUsed] = Flow[Int].map { (in: Int) =>
      -in
    }
    val flow = Flow[Int].map(n => n.toString)

    val sink = Sink.foreach[String](println(_))

    val graph = source.via(flow1).via(flow).to(sink)

    val result = graph.run()

    val s: Source[String, BoundedSourceQueue[String]] = Source.queue[String](100)
    s.runForeach( println(_))

    Future {
      Thread.sleep(2000)
      system.terminate()
    }
  }
}

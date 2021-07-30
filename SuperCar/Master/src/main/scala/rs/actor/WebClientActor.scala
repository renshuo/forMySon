package rs.actor

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Post
import akka.stream.Materializer
import com.typesafe.scalalogging.Logger

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}


object WebClientActor {

  def apply() = {
    Behaviors.setup[String]( ctx => new WebClientActor(ctx).ready())
  }
}

class WebClientActor(ctx: ActorContext[String]) {
  val log = Logger(getClass)

  given executorContext: ExecutionContext = ctx.executionContext
  given materializer: Materializer = Materializer(ctx)
  given system: ActorSystem[String] = ctx.system.asInstanceOf[ActorSystem[String]]

  def ready() = Behaviors.receiveMessage{ (msg: String) =>
    println(s"get web event : ${msg}")

    val url = "http://localhost:8010/car"
    val postReq = Post(url, "{\"Backward\":{\"velocity\":1.3}}")
    Http().singleRequest(postReq).onComplete {
      case Success(resp) => {
        resp.entity.dataBytes
          .map { line =>
            println(line.utf8String)
          }.run()
        // .runForeach( (x: ByteString) => println(x.utf8String))
      }
      case Failure(exception) => println(s"error: ${exception}")
    }
    Behaviors.same

    Behaviors.same
  }
}

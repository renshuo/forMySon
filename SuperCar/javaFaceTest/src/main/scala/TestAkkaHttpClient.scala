import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.client.RequestBuilding.Post
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{FileIO, Framing}
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.ByteString

import java.io.File
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}
import akka.stream.scaladsl.{FileIO, Framing}

import java.nio.charset.Charset


@main def TestClient: Unit = {

  val sys : ActorSystem[String] = ActorSystem(Behaviors.setup[String](ctx => ActorClient().ready()), "system")

  sys.tell("start")
}

class ActorClient {

  def ready(): Behavior[String] = Behaviors.receive { (ctx, msg: String) =>

    println(s"get msg: ${msg}")

    given executorContext: ExecutionContext = ctx.executionContext
    given materializer: Materializer = Materializer(ctx)
    given system: ActorSystem[String] = ctx.system.asInstanceOf[ActorSystem[String]]

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
  }
}
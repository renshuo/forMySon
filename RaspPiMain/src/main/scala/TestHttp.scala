
import akka.actor.Status.{Failure, Success}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.{Http, HttpExt, ServerBuilder}
import akka.http.scaladsl.server.Directives.{as, complete, concat, get, path, pathEnd, pathPrefix, post}

import scala.util.Try

@main def TestHttpMain = {
  val sys = ActorSystem(Behaviors.setup[String](
    ctx =>
      Behaviors.receiveMessage { msg =>
        println(s"get msg: $msg")
        Behaviors.same[String]
      }
  ), "Pi")

  val router = path("test") {
    get {
      complete {
        sys.tell("test")
        "Welcome1."
      }
    }
  }
  val builder: ServerBuilder = Http()(sys).newServerAt("0.0.0.0", 8010)
  val server = builder.bindFlow(Route.toFlow(router)(sys))
  server.onComplete {
    case scala.util.Success(binding) => println(s"server start success. ${binding}")
    case scala.util.Failure(e) => { println(s"error ${e}"); System.exit(0) }
  }(sys.executionContext)

}

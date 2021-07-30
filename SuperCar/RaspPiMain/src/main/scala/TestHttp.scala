
import scala.util.{Failure, Success, Try}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.http.scaladsl.{Http, HttpExt, ServerBuilder}
import akka.http.scaladsl.server.{Directive0, Directives, PathMatcher, Route}
import akka.http.scaladsl.server.Directives.{as, complete, concat, get, method, path, pathEnd, pathPrefix, post}
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.HttpResponse
import rs.actor.{Backward, CarCommand, Forward, Stop, TurnLeft}
import io.circe.parser.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import io.circe.*
import com.typesafe.scalalogging.Logger
import rs.controller.ControlRouter


@main def TestHttpMain = {
  val sys = ActorSystem(Behaviors.setup[String](
    ctx =>
      Behaviors.receiveMessage { msg =>
        println(s"get msg: $msg")
        Behaviors.same[String]
      }
  ), "Pi")

  val router = ControlRouter(null, null).route

  val builder: ServerBuilder = Http()(sys).newServerAt("0.0.0.0", 8010)
  val server = builder.bindFlow(Route.toFlow(router)(sys))
  server.onComplete {
    case Success(binding) => sys.log.debug(s"server start success. ${binding}")
    case Failure(e) => { println(s"error ${e}"); System.exit(0) }
  }(sys.executionContext)

}

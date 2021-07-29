
import scala.util.{Failure, Success, Try}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.{Http, HttpExt, ServerBuilder}
import akka.http.scaladsl.server.{Directive0, Directives, PathMatcher, Route}
import akka.http.scaladsl.server.Directives.{as, complete, concat, get, method, path, pathEnd, pathPrefix, post}
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.HttpResponse
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import spray.json.DefaultJsonProtocol.DoubleJsonFormat
import rs.actor.{Backward, CarCommand, Forward, Stop, TurnLeft}


case class CarCmd(cmd: Forward, ve: Int)

trait JsonSupp extends SprayJsonSupport with  DefaultJsonProtocol {
  implicit val itemFormat: RootJsonFormat[Forward] = jsonFormat1(Forward.apply)
  implicit val backFormat: RootJsonFormat[Backward] = jsonFormat1(Backward.apply)
  implicit val cmdFormat: RootJsonFormat[CarCmd] = jsonFormat2(CarCmd.apply)
}

class RouterManager extends Directives with JsonSupp {
  val route =
    pathPrefix("test") {
      path("car") {
        get {
          complete(Forward(1.0d))
        } ~
          (post | put) {
            entity(as[Backward]) { cmd =>
              println(s"get cmd: ${cmd}")
              complete(CarCmd(Forward(1.0), 1))
            } ~
              entity(as[CarCmd]) { fw =>
                println(s"get cmd: ${fw}")
                complete(fw)
              }
          }
      }
    } ~
    pathPrefix("a" / IntNumber) { v =>
      get {
        complete(s"a/b ${v}")
      }
    } ~
    pathPrefix("tripod" / DoubleNumber / DoubleNumber) { (v, h) =>
      get {
        complete(s" update tripod by ${v}, ${h}")
      }
    } ~
      pathPrefix("car") {
        path("forward" / DoubleNumber) { velocity =>
          complete(s"forward car in ${velocity}")
        } ~
        path("backward" / DoubleNumber){ velocity =>
          complete(s"backward car in ${velocity}")
        }
      }
}


@main def TestHttpMain = {
  val sys = ActorSystem(Behaviors.setup[String](
    ctx =>
      Behaviors.receiveMessage { msg =>
        println(s"get msg: $msg")
        Behaviors.same[String]
      }
  ), "Pi")

  val router = RouterManager().route

  val builder: ServerBuilder = Http()(sys).newServerAt("0.0.0.0", 8010)
  val server = builder.bindFlow(Route.toFlow(router)(sys))
  server.onComplete {
    case Success(binding) => sys.log.debug(s"server start success. ${binding}")
    case Failure(e) => { println(s"error ${e}"); System.exit(0) }
  }(sys.executionContext)

}

package rs.controller

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import rs.actor.*
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.{Http, HttpExt, ServerBuilder}
// import akka.http.scaladsl.server.Directives.{DoubleNumber, IntNumber, as, complete, concat, entity, get, path, pathEnd, pathPrefix, post}
import com.typesafe.scalalogging.Logger
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object WebController {

  def apply(car: ActorRef[CarCommand], tripod: ActorRef[TripodCommand]): Behavior[String]= {
    Behaviors.setup[String](ctx => new WebController(ctx, car, tripod))
  }
}

class WebController(context: ActorContext[String], car: ActorRef[CarCommand], tripod: ActorRef[TripodCommand]) extends AbstractBehavior[String](context) {

  val logger = Logger(getClass)

  override def onMessage(msg: String): Behavior[String] = {
    context.log.info("start web controller.")
    val router = ControlRouter().route
    val builder: ServerBuilder = Http()(context.system).newServerAt("0.0.0.0", 8010)
    val server = builder.bindFlow(Route.toFlow(router)(context.system))
    server.onComplete {
      case scala.util.Success(binding) => println(s"server start success. ${binding}")
      case scala.util.Failure(e) => { println(s"error ${e}"); System.exit(0) }
    }(context.system.executionContext)
    Behaviors.same
  }
}

trait JsonSupp extends SprayJsonSupport with  DefaultJsonProtocol {
  implicit val itemFormat: RootJsonFormat[Forward] = jsonFormat1(Forward.apply)
  implicit val backFormat: RootJsonFormat[Backward] = jsonFormat1(Backward.apply)
  implicit val triUpdateFormat: RootJsonFormat[TripodUpdate] = jsonFormat2(TripodUpdate.apply)
  implicit val triInfoFormat: RootJsonFormat[TripodInfo] = jsonFormat2(TripodInfo.apply)
}

class ControlRouter extends Directives with JsonSupp {
  val route =
    pathPrefix("car") {
      get {
        complete(s"get car info")
      }
        ~
        path("forward") {
          post {
            entity(as[Forward]) { cmd =>
              //
              complete("forward")
            }
          }
        } ~
        path("backward") {
          post {
            entity(as[Backward]) { cmd =>
              //
              complete("back")
            }
          }
        }
    } ~
      pathPrefix("tripod") {
        get {
          complete("tripod state: ")
        }~
          post {
            entity(as[TripodInfo]) { ti =>
              //
              complete(s"set tripod value: ${ti}")
            } ~
              entity(as[TripodUpdate]) { tu =>
                //
                complete(s"update tripod : ${tu}")
              }
          }

      }
}
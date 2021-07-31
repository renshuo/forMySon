package rs.controller

import rs.actor.*

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.{Http, HttpExt, ServerBuilder}
import com.typesafe.scalalogging.Logger

object WebController {

  def apply(car: ActorRef[CarCommand], tripod: ActorRef[TripodCommand]): Behavior[String]= {
    Behaviors.setup[String](ctx => new WebController(ctx, car, tripod))
  }
}

class WebController(context: ActorContext[String], car: ActorRef[CarCommand], tripod: ActorRef[TripodCommand]) extends AbstractBehavior[String](context) {
  val logger = Logger(getClass)

  override def onMessage(msg: String): Behavior[String] = {
    context.log.info("start web controller.")
    val router = ControlRouter(car, tripod).route
    val builder: ServerBuilder = Http()(context.system).newServerAt("0.0.0.0", 8010)
    val server = builder.bindFlow(Route.toFlow(router)(context.system))
    server.onComplete {
      case scala.util.Success(binding) => println(s"server start success. ${binding}")
      case scala.util.Failure(e) => { println(s"error ${e}"); System.exit(0) }
    }(context.system.executionContext)
    Behaviors.same
  }
}

class ControlRouter(car: ActorRef[CarCommand], tripod: ActorRef[TripodCommand]) extends Directives {
  import io.circe.parser._
  import io.circe.generic.auto._
  import io.circe.syntax._
  import io.circe._

  val logger = Logger(getClass)

  val route =
    pathPrefix("car") {
      get {
        complete("get car info: ")
      } ~
      post {
        entity(as[String]) { ent =>
          val decoded = decode[CarCommand](ent)
          decoded match {
            case Left(e) => {
              complete(s"get invalid command: ${e}")
            }
            case Right(carCmd) => {
              logger.info(s"get a post value: ${carCmd} ")
              car.tell(carCmd)
              complete(s"send command ${carCmd} to car.")
            }
          }
        }
      }
    } ~
    pathPrefix("tripod") {
      get {
        complete("tripod state: ")
      }~
      post {
        entity(as[String]) { ent =>
          val decoded = decode[TripodCommand](ent)
          decoded match {
            case Left(e) => {
              complete(s"get invalid command: ${e}")
            }
            case Right(tripodCmd) => {
              logger.info(s"get a post value: ${tripodCmd} ")
              tripod.tell(tripodCmd)
              complete(s"send command ${tripodCmd} to tripod.")
            }
          }
        }
      }
    }
}

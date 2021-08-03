package rs.source

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.{Http, HttpExt, ServerBuilder}
import com.typesafe.scalalogging.Logger
import rs.actor.*

object WebIn {

  def apply(controller: ActorRef[BaseCommand]): Behavior[String]= {
    Behaviors.setup[String](ctx => new WebIn(ctx, controller))
  }
}

class WebIn(context: ActorContext[String], controller: ActorRef[BaseCommand]) extends AbstractBehavior[String](context) {
  val logger = Logger(getClass)

  val ipAddr = "0.0.0.0"
  val port = 8010

  override def onMessage(msg: String): Behavior[String] = {
    context.log.info("start web controller.")
    val router = WebInControlRouter(controller).route
    val builder: ServerBuilder = Http()(context.system).newServerAt(ipAddr, port)
    val server = builder.bindFlow(Route.toFlow(router)(context.system))
    server.onComplete {
      case scala.util.Success(binding) => println(s"server start success. ${binding}")
      case scala.util.Failure(e) => { println(s"error ${e}"); System.exit(0) }
    }(context.system.executionContext)
    Behaviors.same
  }
}

class WebInControlRouter(controller: ActorRef[BaseCommand]) extends Directives {
  import io.circe.*
  import io.circe.generic.auto.*
  import io.circe.parser.*
  import io.circe.syntax.*

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
              controller.tell(carCmd)
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
              controller.tell(tripodCmd)
              complete(s"send command ${tripodCmd} to tripod.")
            }
          }
        }
      }
    }
}

package rs.controller

import akka.actor.typed.{ActorSystem, ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, AbstractBehavior}
import rs.actor._

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.{Http, HttpExt, ServerBuilder}

import akka.http.scaladsl.server.Directives.{as, complete, concat, path, pathEnd, pathPrefix}
import akka.http.scaladsl.server.Directives.{get, post}

object WebController {

  def apply(car: ActorRef[CarCommand], tripod: ActorRef[TripodCommand]): Behavior[String]= {
    Behaviors.setup[String](ctx => new WebController(ctx, car, tripod))
  }
}

class WebController(context: ActorContext[String], car: ActorRef[CarCommand], tripod: ActorRef[TripodCommand]) extends AbstractBehavior[String](context) {

  override def onMessage(msg: String): Behavior[String] = {
    context.log.info("start web controller.")
    val router = path("test") {
      concat(
        get {
          complete {
            "Welcome1."
          }
        },
        post {
          complete {
            "test post"
          }
        }
      )
    }
    val builder: ServerBuilder = Http()(context.system).newServerAt("0.0.0.0", 8010)
    val server = builder.bindFlow(Route.toFlow(router)(context.system))
    server.onComplete {
      case scala.util.Success(binding) => println(s"server start success. ${binding}")
      case scala.util.Failure(e) => { println(s"error ${e}"); System.exit(0) }
    }(context.system.executionContext)
    Behaviors.same
  }
}
package rs.source

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.{Http, HttpExt, ServerBuilder}
import com.typesafe.scalalogging.Logger
import rs.actor.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*

object WebIn {

  import rs.controllerKey
  var controllerList = Set[ActorRef[BaseCommand]]()

  def apply(): Behavior[String]= {
    Behaviors.setup[String] { ctx =>

      val blist = ctx.spawn(Behaviors.receiveMessage{ (list: Receptionist.Listing) =>
        val si: Set[ActorRef[BaseCommand]] = list.serviceInstances(controllerKey)
        this.controllerList = si
        println(s"update actor A list: ${this.controllerList}")
        Behaviors.same
      }, "ctlList")
      ctx.system.receptionist ! Receptionist.Subscribe(controllerKey, blist.ref)

      new WebIn(ctx)
    }
  }
}

class WebIn(context: ActorContext[String]) extends AbstractBehavior[String](context) {
  val logger = Logger(getClass)

  import WebIn.controllerList
  val ipAddr = "0.0.0.0"
  val port = 8010

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
                controllerList.foreach { _.tell(carCmd) }
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
                  controllerList.foreach { _.tell(tripodCmd) }
                  complete(s"send command ${tripodCmd} to tripod.")
                }
              }
            }
          }
      }
    ~ {
      pathPrefix("led") {
        post {
          entity(as[String]) { ent =>
            val decoded = decode[LedCommand](ent)
            decoded match {
              case Left(e) => complete(s"geterror: ${e}")
              case Right(ledCmd) => {
                logger.info(s"get led event: ${ledCmd}")
                controllerList.foreach { _.tell(ledCmd) }
                complete(s"send command ${ledCmd} to controller")
              }
            }
          }
        }
      }
    }

  override def onMessage(msg: String): Behavior[String] = {
    context.log.info("start web controller.")

    val builder: ServerBuilder = Http()(context.system).newServerAt(ipAddr, port)
    val server = builder.bindFlow(Route.toFlow(route)(context.system))
    server.onComplete {
      case scala.util.Success(binding) => println(s"server start success. ${binding}")
      case scala.util.Failure(e) => { println(s"error ${e}"); System.exit(0) }
    }(context.system.executionContext)
    Behaviors.same
  }
}

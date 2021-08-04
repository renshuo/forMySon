package rs

import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import rs.actor.*
import rs.controller.*
import rs.sensor.*
import rs.source.*

import scala.io.StdIn
import org.slf4j.{Logger, LoggerFactory}

val controllerKey = ServiceKey[BaseCommand]("control")

object CarControler {

  val log: Logger = LoggerFactory.getLogger(getClass)

  def apply(): Behavior[String]= {
    log.info("start car controller")
    Behaviors.setup[String](ctx => new CarControler(ctx))
  }
}
class CarControler(ctx: ActorContext[String]) extends AbstractBehavior[String](ctx)  {

  val car: ActorRef[CarCommand] = ctx.spawn(Car().ready(), "car")
  val tripod: ActorRef[TripodCommand] = ctx.spawn(TripodI2C(), "tripod")
  val echo: ActorRef[EchoDirection] = ctx.spawn(SoundEcho(), "echo")
  val cmdSource: ActorRef[String] = ctx.spawn(CmdLineSource(), "cmdIn")
  val mqttSource: ActorRef[String] = ctx.spawn(MqttSub(), "mqtt")
  val webIn: ActorRef[String] = ctx.spawn(WebIn(), "web")


  override def onMessage(msg: String): Behavior[String] = {
    val controller: ActorRef[BaseCommand] = ctx.spawn(DefaultController(car, tripod, echo), "controller")
    msg match {
      case "test" => car.tell(Test())
      case "stop" => car.tell(Stop())
      case _ => {
        ctx.log.info("start Pi")
        cmdSource.tell("start")
        mqttSource.tell("start")
        webIn.tell("start")
      }
    }
    Behaviors.same
  }
}

package rs

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import rs.actor._
import rs.controller._
import rs.sensor._
import rs.source.*

import scala.io.StdIn
import org.slf4j.{Logger, LoggerFactory}

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

  lazy val ctl: Behavior[BaseCommand] = DefaultController(car, tripod)
  val controller: ActorRef[BaseCommand] = ctx.spawn(ctl, "controller")

  val cmdSource: ActorRef[String] = ctx.spawn(CmdLineSource(controller), "cmdIn")
  val mqttSource: ActorRef[String] = ctx.spawn(MqttSub(controller), "mqtt")
  val webIn: ActorRef[String] = ctx.spawn(WebIn(controller), "web")

  private val soundEcho: Behavior[String] = SoundEcho(controller)
  val echo: ActorRef[String] = ctx.spawn(soundEcho, "echo")



  override def onMessage(msg: String): Behavior[String] = {
    msg match {
      case "test" => test()
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

  def test(): Unit = {
    ctx.log.info("start test car: ")
    car.tell(Test())

    println("finish test.")
  }
}

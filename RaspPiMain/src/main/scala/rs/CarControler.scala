package rs

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import rs.actor._
import rs.controller._
import rs.sensor._

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
  val tripod: ActorRef[TripodCommand] = ctx.spawn(TripodI2C().ready(), "tripod")

  val echoController = ctx.spawn(EchoController(car), "echoHandler")
  val cmdLineController = ctx.spawn(CmdLineController(car).start(), "controller")
  val webController = ctx.spawn(WebController(car, tripod), "webCtrl")

  override def onMessage(msg: String): Behavior[String] = {
    msg match {
      case "test" => test()
      case "stop" => car.tell(Stop())
      case _ => {
        ctx.log.info("start Pi")
        cmdLineController.tell("start")
        //echoController.tell("start")
        webController.tell("start")
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

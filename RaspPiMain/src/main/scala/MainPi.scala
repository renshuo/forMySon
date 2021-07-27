import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import rs.actor._
import rs.sensor._

import scala.io.StdIn

@main def MainPi(action: String = "start1") = {
  val sys = ActorSystem(MainPiActor(), "Pi")
  sys.tell(action)
}

object MainPiActor {
  def apply(): Behavior[String]= {
    Behaviors.setup[String](ctx => new MainPiActor(ctx))
  }
}
class MainPiActor(ctx: ActorContext[String]) extends AbstractBehavior[String](ctx)  {

  val car: ActorRef[CarCommand] = ctx.spawn(Car().ready(), "car")
  val tripod: ActorRef[TripodUpdate] = ctx.spawn(TripodI2C().ready(), "tripod")

  val echoHandler = ctx.spawn(Behaviors.receiveMessage[Double] { distance =>
    println(s"get distance : ${distance}")
    if (distance < 15) {
      car.tell(CarCommand.Stop)
      Thread.sleep(200)
      car.tell(CarCommand.Backward)
      Thread.sleep(1500)
      car.tell(CarCommand.Stop)
      Thread.sleep(200)
      car.tell(CarCommand.TurnLeft)
      Thread.sleep(1500)
      car.tell(CarCommand.Stop)
      Thread.sleep(200)
      car.tell(CarCommand.Forward)
    }
    Thread.sleep(500)
    echo.tell("checkDistance")
    Behaviors.same
  }, "echoHandler")

  val echo: ActorRef[String] = ctx.spawn(new SoundEcho(echoHandler).ready(), "echo")

  val controller = ctx.spawn( Behaviors.receiveMessage[String] { msg =>
    println("start controller")
    while(true) {
      try {
        val cmd: String = StdIn.readLine()
        cmd match {
          case _ if cmd.startsWith("car") => {
            val carCmd = cmd.split(" ")
            if (carCmd.size > 1) {
              car.tell(CarCommand.valueOf(carCmd(1)))
            } else {
              println("no command")
            }
          }
          case "stop" => {
            car.tell(CarCommand.Stop)
            System.exit(0)
          }
          case _ => println(s"get command $cmd")
        }
      } catch {
        case _ => println("error. next.")
      }
    }
    Behaviors.same
  }, "controller")

  override def onMessage(msg: String): Behavior[String] = {
    msg match {
      case "test" => test()
      case "stop" => car.tell(CarCommand.Stop)
      case _ => {
        ctx.log.info("start Pi")
        controller.tell("start")
        echo.tell("start")
      }
    }
    Behaviors.same
  }

  def test(): Unit = {
    ctx.log.info("start test car: ")
    //echo.tell("start")
    car.tell(CarCommand.Test)

    println("finish test.")
  }
}



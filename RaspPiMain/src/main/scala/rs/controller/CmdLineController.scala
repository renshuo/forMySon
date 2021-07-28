package rs.controller

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import rs.actor._
import rs.sensor._

import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn

class CmdLineController(car: ActorRef[CarCommand]) {

  def start():Behavior[String] = Behaviors.receive[String] { (ctx, msg) =>
    ctx.log.info("start cmd line controller")
    given ec: ExecutionContext = ctx.executionContext
    Future {
      while (true) {
        try {
          val cmd: String = StdIn.readLine()
          cmd match {
            case _ if cmd.startsWith("car") => {
              val carCmd = cmd.split(" ")
              if (carCmd.size > 1) {
                carCmd(1) match {
                  case "stop" => car.tell(Stop())
                  case "forward" => car.tell(Forward(40))
                  case "backward" => car.tell(Backward(40))
                  case "left" => car.tell(TurnLeft(30))
                  case "right" => car.tell(TurnRight(30))
                }
              } else {
                println("no command")
              }
            }
            case "stop" => {
              car.tell(Stop())
              System.exit(0)
            }
            case _ => println(s"get command $cmd")
          }
        } catch {
          case _ => println("error. next.")
        }
      }
    }
    Behaviors.same
  }
}

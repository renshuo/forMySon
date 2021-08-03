package rs.source

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import rs.actor.*
import rs.sensor.*

import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn

object CmdLineSource {

  def apply(controller: ActorRef[BaseCommand]): Behavior[String] = {
    Behaviors.setup[String]( ctx => new CmdLineSource(ctx, controller).start())
  }
}

class CmdLineSource(ctx: ActorContext[String], controller: ActorRef[BaseCommand]) {

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
                  case "stop" => controller.tell(Stop())
                  case "forward" => controller.tell(Forward(40))
                  case "backward" => controller.tell(Backward(40))
                  case "left" => controller.tell(TurnLeft(30))
                  case "right" => controller.tell(TurnRight(30))
                }
              } else {
                println("no command")
              }
            }
            case "stop" => {
              controller.tell(Stop())
              System.exit(0)
            }
            case _ if cmd.startsWith("tripod") => {
              val tripodCmd = cmd.split(" ")
              controller.tell(TripodInfo(tripodCmd(1).toDouble, tripodCmd(2).toDouble))
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

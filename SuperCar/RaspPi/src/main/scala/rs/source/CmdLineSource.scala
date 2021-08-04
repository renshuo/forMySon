package rs.source

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import rs.actor.*
import rs.sensor.*

import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn

object CmdLineSource {
  import rs.controllerKey

  var controllerList = Set[ActorRef[BaseCommand]]()

  def apply(): Behavior[String] = {
    Behaviors.setup[String]{ ctx =>

      val blist = ctx.spawn(Behaviors.receiveMessage{ (list: Receptionist.Listing) =>
        val si: Set[ActorRef[BaseCommand]] = list.serviceInstances(controllerKey)
        this.controllerList = si
        println(s"update actor A list: ${this.controllerList}")
        Behaviors.same
      }, "ctlList")
      ctx.system.receptionist ! Receptionist.Subscribe(controllerKey, blist.ref)

      new CmdLineSource(ctx).start()
    }
  }
}

class CmdLineSource(ctx: ActorContext[String]) {

  def tellController(baseCommand: BaseCommand): Unit = {
    import CmdLineSource.controllerList
    controllerList.foreach( ref =>
      ref.tell(baseCommand)
    )
  }

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
                  case "stop" => tellController(Stop())
                  case "forward" => tellController(Forward(40))
                  case "backward" => tellController(Backward(40))
                  case "left" => tellController(TurnLeft(30))
                  case "right" => tellController(TurnRight(30))
                }
              } else {
                println("no command")
              }
            }
            case "stop" => {
              tellController(Stop())
              System.exit(0)
            }
            case _ if cmd.startsWith("tripod") => {
              val tripodCmd = cmd.split(" ")
              tellController(TripodInfo(tripodCmd(1).toDouble, tripodCmd(2).toDouble))
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

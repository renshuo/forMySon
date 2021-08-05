import akka.actor.typed.SupervisorStrategy.Stop
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorSystem, Behavior, PostStop}
import akka.util.Timeout

import java.util.concurrent.TimeUnit
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

@main def TestActor() = {
  val sys = ActorSystem(MainAct(), "test")
  sys.tell("test")

}


object MainAct {
  def apply(): Behavior[String] = Behaviors.setup{ ctx =>

    val new1 = ctx.spawn(TestActorState(), "new1")


    Behaviors.receiveMessage[String] { x =>
      println(s"context:x: ${x}")
      new1.tell("start")
      ctx.scheduleOnce(FiniteDuration(1, TimeUnit.SECONDS), new1, "msg")
      ctx.scheduleOnce(FiniteDuration(2, TimeUnit.SECONDS), new1, "init")
      ctx.scheduleOnce(FiniteDuration(3, TimeUnit.SECONDS), new1, "msg")
      ctx.scheduleOnce(FiniteDuration(4, TimeUnit.SECONDS), new1, "stop")

      given e : ExecutionContext = ctx.executionContext
      Future {
        Thread.sleep(6000)
        println("stop system.")
        ctx.system.terminate()
      }
      Behaviors.same
    }
  }
}


object TestActorState {
  def apply(): Behavior[String] = Behaviors.setup { ctx =>
    new TestActorState(ctx).init()
  }
}

class TestActorState(ctx: ActorContext[String]) {

  def init(): Behavior[String] = Behaviors.receiveMessage[String] {
    case "start" =>
      println("turn to start")
      start()
    case "stop" => {
      stop()
    }
    case msg => {
      println(s"in init get msg: ${msg}")
      Behaviors.same
    }
  }.receiveSignal {
    case (_, PostStop) => {
      println("in post stop")
      Behaviors.same
    }
  }

  def start():Behavior[String] = Behaviors.receiveMessage[String] {
    case "init" =>
      println("turn to init")
      init()
    case "stop" => {
      stop()
    }
    case msg => {
      println(s"in start : ${msg}")
      Behaviors.same
    }
  }

  def stop(): Behavior[String] = Behaviors.stopped { () =>
    println("stop actor")
  }
}
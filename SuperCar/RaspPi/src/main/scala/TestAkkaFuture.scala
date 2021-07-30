import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.{ExecutionContext, Future}

@main def TestAkkaFuture = {
  val system = ActorSystem(Behaviors.setup { ctx =>
    Behaviors.receiveMessage { msg =>


      println(s"$msg hello world")
      ctx.log.info("log in ctx")
      println("after ctx log")


      Behaviors.same
    }
  }, "system")

  system.tell("start")
}


@main def TestScalaFuture = {
  given executor: ExecutionContext = ExecutionContext.global
  println("start test future")
  for (i <- 1 to 2) {
    Future {
      Thread.sleep(1000)
      println(s"${System.currentTimeMillis()}: in future $i: ${Thread.currentThread()}")
    }
  }
  Thread.sleep(2000)
  println("after test")
}
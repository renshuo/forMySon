import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

@main def receptionTest: Unit = {
  val system = ActorSystem.create(Behaviors.setup[String]( (ctx:ActorContext[String]) => Behaviors.receiveMessage{ msg =>
    val b = ctx.spawn(ActorB(), "b")
    val a = ctx.spawn(ActorA(), "a")
    val a2= ctx.spawn(ActorA(), "a2")
    println(s"create a,b: ${a}, ${b} ")
    Behaviors.same
  }), "sys")
  system.tell("start")
}

val key = ServiceKey[String]("test")

object ActorA {
  def apply() = Behaviors.setup[String]{ctx =>
    ctx.system.receptionist.tell(Receptionist.register(key, ctx.self))
    new ActorA(ctx)
  }
}

class ActorA(ctx: ActorContext[String]) extends AbstractBehavior(ctx) {
  override def onMessage(msg: String): Behavior[String] = {
    println(s"A get msg: ${msg} ")
    Behaviors.same
  }

}


object ActorB {

  var someA = Set[ActorRef[String]]()

  def apply() = {
    Behaviors.setup[String]{ctx =>

      val blist = ctx.spawn(Behaviors.receiveMessage{ (list: Receptionist.Listing) =>
        val si: Set[ActorRef[String]] = list.serviceInstances(key)
        this.someA = si
        println(s"update actor A list: ${this.someA}")
        Behaviors.same
      }, "blist")
      ctx.system.receptionist ! Receptionist.Subscribe(key, blist.ref)

      Behaviors.withTimers { ts =>
        ts.startTimerWithFixedDelay("timer", FiniteDuration(1, TimeUnit.SECONDS))
        new ActorB().start()
      }
    }
  }
}

class ActorB {
import ActorB.someA

  def start() = Behaviors.receive { (ctx: ActorContext[String], msg: String) =>
    println(s"get common msg: ${msg} , actorA is ${ActorB.someA}")
    ctx.spawn(ActorA(), s"newA${someA.size}")
    Behaviors.same
  }
}


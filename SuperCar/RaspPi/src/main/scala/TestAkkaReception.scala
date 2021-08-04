import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

@main def receptionTest: Unit = {
  val system = ActorSystem.create(Behaviors.setup[String]( (ctx:ActorContext[String]) => Behaviors.receiveMessage{ msg =>
    val a = ctx.spawn(ActorA(), "a")
    val a2= ctx.spawn(ActorA(), "a2")
    val b = ctx.spawn(ActorB(), "b")
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
    Behaviors.setup[Receptionist.Listing | String]{ctx =>
      ctx.system.receptionist ! Receptionist.Subscribe(key, ctx.self)

      Behaviors.receiveMessage[Receptionist.Listing| String] {
        case key.Listing(listings) => {
          println(s"list changed: ${listings}")
          Behaviors.same
        }
        case x:String => {
          println(s"here are actor A: ${someA}")
          Behaviors.same
        }
      }
    }
  }
}


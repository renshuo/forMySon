import akka.actor.typed.ActorSystem
import akka.actor.typed.javadsl.Behaviors

import java.time.Duration

@main def TestAkkaTimer = {
  ActorSystem.create(Behaviors.setup{ ctx =>
    Behaviors.withTimers { timer =>
      timer.startTimerAtFixedRate("", Duration.ofSeconds(1))
      Behaviors.receiveMessage { msg =>
        println("hello world")
        Behaviors.same
      }
    }
  }, "system").tell("start")
}
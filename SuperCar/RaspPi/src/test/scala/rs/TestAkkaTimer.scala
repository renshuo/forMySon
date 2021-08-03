import akka.actor.typed.ActorSystem
import akka.actor.typed.javadsl.Behaviors
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.Duration


class AkkaTimerTest extends AnyFlatSpec with Matchers {

  "timer test" should "send event in fixed rate" in {
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
}


import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import rs.actor.*
import rs.sensor.*
import rs.CarControler

import scala.io.StdIn

@main def MainPi(action: String = "start1") = {
  val sys = ActorSystem(CarControler(), "Pi")
  sys.tell(action)
}





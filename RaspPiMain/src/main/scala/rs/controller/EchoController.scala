package rs.controller

import akka.actor.typed.scaladsl.{ActorContext, AbstractBehavior, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import rs.actor._
import rs.sensor._


object EchoController {

  def apply(car: ActorRef[CarCommand]): Behavior[String]= {
    Behaviors.setup[String](ctx => new EchoController(ctx, car))
  }
}

class EchoController(context: ActorContext[String], car: ActorRef[CarCommand]) extends AbstractBehavior[String](context) {


  val handler = context.spawn( Behaviors.receiveMessage[Double] { distance =>
    handleDistance(distance)
    Behaviors.same
  }, "handler")

  def handleDistance(distance: Double): Unit = {
    println(s"get distance : ${distance}")
    if (distance < 15) {
      car.tell(Stop())
      Thread.sleep(200)
      car.tell(Backward(40))
      Thread.sleep(1500)
      car.tell(Stop())
      Thread.sleep(200)
      car.tell(TurnLeft(60))
      Thread.sleep(1500)
      car.tell(Stop())
      Thread.sleep(200)
      car.tell(Forward(40))
    }
    Thread.sleep(500)
  }

  val echo: ActorRef[String] = context.spawn(SoundEcho(handler), "echo")

  override def onMessage(msg: String): Behavior[String] = {

    Behaviors.same
  }
}

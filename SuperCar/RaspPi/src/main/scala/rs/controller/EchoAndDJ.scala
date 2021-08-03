import EchoAndDjController.log
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.typesafe.scalalogging.Logger
import rs.actor.{BaseCommand, CarCommand, TripodCommand}
import rs.dev.I2cDev


object EchoAndDjController {

  val log = Logger(getClass)

  def apply(car: ActorRef[CarCommand], tripod: ActorRef[TripodCommand]): Behavior[BaseCommand]= {
    log.info("create Echo controller")
    Behaviors.setup[BaseCommand](ctx => new EchoAndDjController(ctx, car, tripod))
  }
}


class EchoAndDjController(ctx: ActorContext[BaseCommand], car: ActorRef[CarCommand], tripod: ActorRef[TripodCommand]) extends AbstractBehavior[BaseCommand](ctx) {


  override def onMessage(msg: BaseCommand): Behavior[BaseCommand] = {
    log.info(s"get msg: ${msg}")
    Behaviors.same
  }
}

@main def EchoAndDj(): Unit ={
  val dev = I2cDev

  var djDegree = 140
  var direction = 1
  var isObject = 0
  dev.setPwmRate(2, degreeToRate(djDegree.toDouble))
  while(true) {
    val distance = TestEchoObj.getDistance()
    if (distance<20.0) {
      isObject = 1
      {
        dev.setPwmRate(2, degreeToRate((djDegree-5).toDouble))
        val d2 = TestEchoObj.getDistance()
        if (d2>20) {
          dev.setPwmRate(2, degreeToRate((djDegree+5).toDouble))
        }
      }
    } else {
      djDegree += direction
      if (djDegree == 180) {
        direction = -1
      } else if (djDegree == 0) {
        direction = 1
      }
      dev.setPwmRate(2, degreeToRate(djDegree.toDouble))
    }
    Thread.sleep(50)
  }
}
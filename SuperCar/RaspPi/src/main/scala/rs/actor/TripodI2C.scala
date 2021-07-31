package rs.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import com.typesafe.scalalogging.Logger
import rs.dev.I2cDev

import scala.concurrent.{ExecutionContext, Future}

object TripodI2C{

  def apply():Behavior[TripodCommand] = {
    Behaviors.setup[TripodCommand]( ctx => new TripodI2C(ctx).ready())
  }
}

class TripodI2C(ctx: ActorContext[TripodCommand]) {

  given ctx1: ActorContext[TripodCommand] = ctx
  given executionContext :ExecutionContext = ctx.executionContext

  val log = Logger(getClass)

  var pitchingDegree = 0.0
  pitchingSet(90.0)
  var directionDegree = 0.0
  directionSet(90.0)
  var pitchingVelocity = 0.0
  var directionVelocity = 0.0

  val pitchingPort = 0
  val directionPort = 1

  /**
   * 0 -- 180 degree -> 0.5ms -- 2.5ms /20ms -> 2.5 -- 12.5 rate
   */
  def degreeToRate(degree: Double): Double = 2.5 + degree /180 *10

  private def pitchingSet(newPitching: Double) = {
    if (newPitching < 0 || newPitching > 180) {
      println(s"pitchingNew is $newPitching ignore, out of (0 - 180)")
    } else if (Math.abs(pitchingDegree-newPitching)<0.2) {
    } else {
      pitchingDegree = newPitching
      I2cDev.setPwmRate(pitchingPort, degreeToRate(newPitching))
    }
  }

  def directionSet(newDirection: Double): Unit = {
    if (newDirection < 0 || newDirection > 180) {
      println(s"directionNew is $newDirection, ignore, out of (0 - 180)")
    } else if (Math.abs(directionDegree-newDirection)<0.2) {
    } else {
      directionDegree = newDirection
      I2cDev.setPwmRate(directionPort, degreeToRate(newDirection))
    }
  }

  Future {
    while (true) {
      //println(s"update velocify : ${pitchingVelocity}, ${directionVelocity}")
      try{
        if (Math.abs(pitchingVelocity) > 0.2) pitchingSet(pitchingDegree + pitchingVelocity)
        if (Math.abs(directionVelocity) > 0.2) directionSet(directionDegree + directionVelocity)
      } catch {
        case e => println(s"${e}")
      }
      Thread.sleep(40)
    }
  }

  def ready(): Behavior[TripodCommand] = Behaviors.receive { (ctx, msg: TripodCommand) =>
    log.debug(s"get tripod command : ${msg}")
    given context: ActorContext[TripodCommand] = ctx
    msg match {
      case TripodUpdate(v, h, delay) => {
        pitchingSet(pitchingDegree + v)
        directionSet(directionDegree + h)
      }
      case TripodInfo(pitch, direction) => {
        pitchingSet(pitch)
        directionSet(direction)
      }
      case TripodVelocity(pitchingVelocity, directionVelocity) => {
        this.pitchingVelocity = pitchingVelocity
        this.directionVelocity = directionVelocity
      }
    }
    Behaviors.same
  }
}



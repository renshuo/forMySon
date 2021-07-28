package rs.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import com.pi4j.io.i2c.{I2CBus, I2CDevice, I2CFactory}

class TripodI2C {

  var pitchingDegree = 90.0
  var directionDegree = 90.0

  val pitchingPort = 0
  val directionPort = 1

  def degreeToTime(degree: Float): Float = 0.5f + degree/180 * 2
  def degreeToTime(degree: Double): Double = 0.5d + degree/180 * 2

  def direct(pitching: Double, direction: Double)(using ctx: ActorContext[TripodCommand]): Unit = {
    if (pitching < 0 || pitching > 180) {
      ctx.log.warn(s"pitchingNew is $pitching ignore, out of (0 - 180)")
    } else {
      I2cDev.setPwm(pitchingPort, degreeToTime(pitching))
      pitchingDegree = pitching
    }
    if (direction < 0 || direction > 180) {
      ctx.log.warn(s"directionNew is $direction, ignore, out of (0 - 180)")
    } else {
      I2cDev.setPwm(directionPort, degreeToTime(direction))
      directionDegree = direction
    }
    Thread.sleep(40) //设定舵机在40ms内完成转向工作
    I2cDev.setPwm(pitchingPort, 0) // pwm清零可以避免pwm信号导致的舵机抖动问题
    I2cDev.setPwm(directionPort, 0)
  }

  def ready(): Behavior[TripodCommand] = Behaviors.receive { (ctx, msg: TripodCommand) =>
    given context: ActorContext[TripodCommand] = ctx
    msg match {
      case TripodUpdate(v, h) => direct(pitchingDegree + v, directionDegree + h)
      case TripodInfo(pitch, direction) => direct(pitch, direction)
    }
    Behaviors.same
  }
}



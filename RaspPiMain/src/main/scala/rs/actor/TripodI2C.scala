package rs.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import com.pi4j.io.i2c.{I2CBus, I2CDevice, I2CFactory}

class TripodI2C {

  var pitchingDegree = 90.0
  var directionDegree = 90.0

  val pitchingPort = 0
  val directionPort = 1

  val dev = I2cDev

  def degreeToTime(degree: Float): Float = 0.5f + degree/180 * 2
  def degreeToTime(degree: Double): Double = 0.5d + degree/180 * 2

  def ready(): Behavior[TripodUpdate] = Behaviors.receive { (ctx, msg: TripodUpdate) =>
    val pitchingNew = pitchingDegree + msg.v
    val directionNew = directionDegree + msg.h

    if (pitchingNew < 0 || pitchingNew > 180) {
      ctx.log.warn(s"pitchingNew is $pitchingNew, ignore update ${msg.v}")
    } else {
      dev.setPwm(pitchingPort, degreeToTime(pitchingNew))
      pitchingDegree = pitchingNew
    }

    if (directionNew < 0 || directionNew > 180) {
      ctx.log.warn(s"directionNew is $directionNew, ignore update ${msg.h}")
    } else {
      dev.setPwm(directionPort, degreeToTime(directionNew))
      directionDegree = directionNew
    }
    Thread.sleep(40) //设定舵机在40ms内完成转向工作
    dev.setPwm(pitchingPort, 0) // pwm清零可以避免pwm信号导致的舵机抖动问题
    dev.setPwm(directionPort, 0)
    Behaviors.same
  }
}



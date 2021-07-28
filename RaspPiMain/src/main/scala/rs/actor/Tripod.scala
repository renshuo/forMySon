package rs.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import rs.actor.GpioDevPwm

class Tripod {

  val pitch = GpioDevPwm(23)
  val direct = GpioDevPwm(24)

  var pitchingDegree = 90.0
  var directionDegree = 90.0

  def directDo(): Behavior[TripodInfo] = Behaviors.receive { (ctx, msg:TripodInfo) =>
    println(s"set to ${msg}")
    pitch.setPwm((25.0 + msg.pitching * 100.0 / 180.0).ceil.toInt)
    direct.setPwm((25.0 + msg.direction * 100.0 / 180.0).ceil.toInt)
    Thread.sleep(40) //设定舵机在40ms内完成转向工作
    pitch.setPwm(0) // pwm清零可以避免pwm信号导致的舵机抖动问题
    direct.setPwm(0)

    Behaviors.same
  }

  def ready(): Behavior[TripodUpdate] = Behaviors.receive { (ctx, msg: TripodUpdate) =>
    val pitchingNew = pitchingDegree + msg.v
    val directionNew = directionDegree + msg.h

    if (pitchingNew < 0 || pitchingNew > 180) {
      ctx.log.warn(s"pitchingNew is $pitchingNew, ignore update ${msg.v}")
    } else {
      /**
       * 9g舵机的角度计算：
       * 舵机的有效范围是50Hz pwm, 0.5ms-2.5ms有效（0-180度）
       * 即20ms(1s/50Hz=20ms)的周期内，0.5ms-2.5ms的高电平，其余为低电平
       * 换算占空比为 0.5ms/20ms = 2.5% -- 2.5ms/20ms=12.5%
       * 按照之前的range配置，则 setPwm(25) -- setPwm(125)
       * 根据角度计算的公式为: setPwm(25+ deg/180*100)
       */
      pitch.setPwm((25.0 + pitchingNew.toFloat * 100.0 / 180.0).ceil.toInt)
      pitchingDegree = pitchingNew
    }

    if (directionNew < 0 || directionNew > 180) {
      ctx.log.warn(s"directionNew is $directionNew, ignore update ${msg.h}")
    } else {
      direct.setPwm((25.0 + directionNew.toFloat * 100.0 / 180.0).ceil.toInt)
      directionDegree = directionNew
    }

    Thread.sleep(40) //设定舵机在40ms内完成转向工作
    pitch.setPwm(0) // pwm清零可以避免pwm信号导致的舵机抖动问题
    direct.setPwm(0)

    Behaviors.same
  }
}
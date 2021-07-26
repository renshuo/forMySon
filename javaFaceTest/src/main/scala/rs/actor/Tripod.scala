package rs.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import com.pi4j.io.gpio.event.{GpioPinDigitalStateChangeEvent, GpioPinListener, GpioPinListenerDigital}
import com.pi4j.io.gpio.{GpioFactory, GpioController, RaspiPin, GpioPinPwmOutput, Pin}

case class TripodUpdate(v: Double, h: Double)
case class TripodInfo(pitching: Double, direction: Double)

class Tripod {

  val gpio: GpioController = GpioFactory.getInstance()
  val pitching = gpio.provisionPwmOutputPin(RaspiPin.GPIO_23, 0)
  val direction = gpio.provisionPwmOutputPin(RaspiPin.GPIO_24, 0)
  println(s"get pwm pin: $pitching $direction")
  com.pi4j.wiringpi.Gpio.pwmSetMode(com.pi4j.wiringpi.Gpio.PWM_MODE_MS)
  /**
   * PI的PWM频率计算方法：
   * pi的PWM频率是 19.2MHz 即 19200_000
   * 所以 19200_000 = Range * ClockDiver * Rate
   * 假设要做50Hz的PWM, rate = 50, Range是setPwm的取值范围，假设取 1-1000
   * 则 clockDiver = 19200_000 / 1000 / 50 = 384
   */
  com.pi4j.wiringpi.Gpio.pwmSetRange(1000)
  com.pi4j.wiringpi.Gpio.pwmSetClock(384)

  var pitchingDegree = 90.0
  var directionDegree = 90.0

  def directDo(): Behavior[TripodInfo] = Behaviors.receive { (ctx, msg:TripodInfo) =>
    println(s"set to ${msg}")
    pitching.setPwm((25.0 + msg.pitching * 100.0 / 180.0).ceil.toInt)
    direction.setPwm((25.0 + msg.direction * 100.0 / 180.0).ceil.toInt)
    Thread.sleep(40) //设定舵机在40ms内完成转向工作
    pitching.setPwm(0) // pwm清零可以避免pwm信号导致的舵机抖动问题
    direction.setPwm(0)

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
      pitching.setPwm((25.0 + pitchingNew.toFloat * 100.0 / 180.0).ceil.toInt)
      pitchingDegree = pitchingNew
    }

    if (directionNew < 0 || directionNew > 180) {
      ctx.log.warn(s"directionNew is $directionNew, ignore update ${msg.h}")
    } else {
      direction.setPwm((25.0 + directionNew.toFloat * 100.0 / 180.0).ceil.toInt)
      directionDegree = directionNew
    }

    Thread.sleep(40) //设定舵机在40ms内完成转向工作
    pitching.setPwm(0) // pwm清零可以避免pwm信号导致的舵机抖动问题
    direction.setPwm(0)

    Behaviors.same
  }
}
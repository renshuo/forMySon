import com.pi4j.io.gpio._
import com.pi4j.io.gpio.event.{GpioPinDigitalStateChangeEvent, GpioPinListener, GpioPinListenerDigital}
import com.pi4j.io.gpio.trigger.GpioCallbackTrigger
import com.pi4j.util.CommandArgumentParser

import scala.io.StdIn


@main
def TestDJ(port: Int, sleepTime: Int= 40, clock: Int, range: Int) = {
  val pin = RaspiPin.getPinByAddress(port)
  val gpio: GpioController  = GpioFactory.getInstance()
  println(s"get gpio controller: $gpio")
  val sig = if (Array(1, 23, 24, 26).contains(port)) {
    gpio.provisionPwmOutputPin(pin, 0)
  } else {
    gpio.provisionSoftPwmOutputPin(pin, 0)
  }
  println(s"get pwm pin $sig")
  com.pi4j.wiringpi.Gpio.pwmSetMode(com.pi4j.wiringpi.Gpio.PWM_MODE_MS)
  com.pi4j.wiringpi.Gpio.pwmSetRange(1000)
  com.pi4j.wiringpi.Gpio.pwmSetClock(384)
  /**
   * PI的PWM频率计算方法：
   * pi的PWM频率是 19.2MHz 即 19200_000
   * 所以 19200_000 = Range * ClockDiver * Rate
   * 假设要做50Hz的PWM, rate = 50, Range是setPwm的取值范围，假设取 1-1000
   * 则 clockDiver = 19200_000 / 1000 / 50 = 384
   */
  /**
   * 9g舵机的角度计算：
   * 舵机的有效范围是50Hz pwm, 0.5ms-2.5ms有效（0-180度）
   * 即20ms(1s/50Hz=20ms)的周期内，0.5ms-2.5ms的高电平，其余为低电平
   * 换算占空比为 0.5ms/20ms = 2.5% -- 2.5ms/20ms=12.5%
   * 按照之前的range配置，则 setPwm(25) -- setPwm(125)
   * 根据角度计算的公式为: setPwm(25+ deg/180*100)
   */

  try{
    while (true) {
      print("input degree: ")
      val dgree = StdIn.readInt()
      println(s"set dj to ${dgree}")
      sig.setPwm((25.0 + dgree.toFloat * 100.0 / 180.0).ceil.toInt)
      //sig.setPwm(dgree)
      Thread.sleep( sleepTime) //设定舵机在40ms内完成转向工作
      sig.setPwm(0) // pwm清零可以避免pwm信号导致的舵机抖动问题
    }
  }catch {
    case _ => { gpio.shutdown(); println("shutdown gpio") }
  }

}
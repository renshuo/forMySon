
import com.pi4j.io.i2c.{I2CBus, I2CDevice, I2CFactory}

import scala.io.StdIn
import rs.actor.I2cDev

@main
def TestDJ2(): Unit = {
  val dev = I2cDev(1, 0x40, 50)

  val port = 0
  while(true) {
    val degree = StdIn.readLine()
    val vs = degree.split(",").map { _.toInt }
    dev.setPwm(port, degreeToTime(vs(0).toFloat))
    dev.setPwm(port+1, degreeToTime(vs(1).toFloat))
    Thread.sleep(40)
    dev.setPwm(port, 0)
    dev.setPwm(port+1, 0)
  }
}


/**
 * 舵机： 50Hz -> 20ms
 *
 * 0.5ms ~ 2.5ms
 */
def degreeToTime(degree: Float): Float = 0.5f + degree/180 * 2



import com.pi4j.io.i2c.{I2CBus, I2CDevice, I2CFactory}
import rs.dev.I2cDev

import scala.io.StdIn

@main
def TestDJ2(): Unit = {
  val dev = I2cDev

  val port = 0
  while(true) {
    val degree = StdIn.readLine()
    val vs = degree.split(",").map { _.toInt }
    dev.setPwmRate(port, degreeToRate(vs(0).toDouble))
    dev.setPwmRate(port+1, degreeToRate(vs(1).toDouble))
    Thread.sleep(40)
    dev.setPwmRate(port, 0)
    dev.setPwmRate(port+1, 0)
  }
}


/**
 * 舵机： 50Hz -> 20ms
 *
 * 0.5ms ~ 2.5ms -> 2.5 ~ 12.5 %
 */
def degreeToRate(degree: Double): Double = 2.5 + degree /180 *10


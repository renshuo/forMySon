
import com.pi4j.io.i2c.{I2CBus, I2CDevice, I2CFactory}

import scala.io.StdIn

extension (b: Byte){
  def &(n: Int): Byte = (b & n).toByte
  def |(n: Int): Byte = (b | n).toByte
}

extension (dev: I2CDevice) {
  def write(addr: Int, value: Int): Unit = dev.write(addr, value.toByte)
  def write(addr: Int, value: Long): Unit = dev.write(addr, value.toByte)
}

@main
def TestDJ2(): Unit = {
  val i2c: I2CBus = I2CFactory.getInstance(1)
  println(s"get i2c bus: ${i2c}")
  val dev: I2CDevice = i2c.getDevice(0x40)
  initMode(dev)

  val addr1 = 0x06
  while(true) {
    val degree = StdIn.readInt()
    setPwm(dev, addr1, degreeToTime(degree.toFloat))
    Thread.sleep(40)
    setPwm(dev, addr1, 0)
  }
}

def initMode(dev: I2CDevice): Unit = {
  val oldMode = dev.read(0)
  val newmode = (oldMode & 0x7f | 0x10) // RESTART=0, SLEEP=1
  dev.write(0, newmode) // go to sleep

  val freq = 50
  val prescale = (25_000_000 / 4096.0 / freq).round -1
  dev.write(0xFE, prescale) // update prescale by 50Hz

  dev.write(0, oldMode) // weak up
  Thread.sleep(5)
  dev.write(0, oldMode | 0x80) // set RESTART bit to 1 to restart pwm channels
  println(s"finish init mode and set freq: ${freq}")
}

/**
 * 舵机： 50Hz -> 20ms
 *
 * 0.5ms ~ 2.5ms
 */
def degreeToTime(degree: Float): Float = 0.5f + degree/180 * 2

def setPwm(dev: I2CDevice, addr: Int, offTime: Float): Unit = {
  val begin = 0
  val offStamp = (offTime /20 * 4096).round
  println(s"set to offtimestamp: $offStamp")
  dev.write(addr, begin)
  dev.write(addr+1, begin)
  dev.write(addr+2, offStamp & 0xff)
  dev.write(addr+3, offStamp >> 8)
}
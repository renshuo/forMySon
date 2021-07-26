package rs.actor

import com.pi4j.io.i2c.{I2CBus, I2CDevice, I2CFactory}

class I2cDev(busNumber: Int, devAddr: Int, freq: Int) {

  extension (b: Byte) {
    def &(n: Int): Byte = (b & n).toByte
    def |(n: Int): Byte = (b | n).toByte
  }

  extension (dev: I2CDevice) {
    def write(addr: Int, value: Int): Unit = dev.write(addr, value.toByte)
    def write(addr: Int, value: Long): Unit = dev.write(addr, value.toByte)
  }

  val i2c: I2CBus = I2CFactory.getInstance(busNumber)
  val dev: I2CDevice = i2c.getDevice(devAddr)
  {
    val oldMode = dev.read(0)
    val newmode = (oldMode & 0x7f | 0x10) // RESTART=0, SLEEP=1
    dev.write(0, newmode) // go to sleep

    val prescale = (25_000_000 / 4096.0 / freq).round - 1
    dev.write(0xFE, prescale) // update prescale by 50Hz

    dev.write(0, oldMode) // weak up
    Thread.sleep(5)
    dev.write(0, oldMode | 0x80) // set RESTART bit to 1 to restart pwm channels
    println(s"finish init mode and set freq: ${freq}")
  }

  def setPwm(portNum: Int, offTime: Double): Unit = {
    setPwm(portNum, offTime.toFloat)
  }

  def setPwm(portNum: Int, offTime: Float): Unit = {
    val begin = 0
    val offStamp = (offTime / 20 * 4096).round
    val addr = 0x06 + 4*portNum
    println(s"set to offtimestamp: $offStamp")
    dev.write(addr, begin)
    dev.write(addr + 1, begin)
    dev.write(addr + 2, offStamp & 0xff)
    dev.write(addr + 3, offStamp >> 8)
  }

}

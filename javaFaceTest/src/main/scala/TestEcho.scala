import com.pi4j.io.gpio._
import com.pi4j.io.gpio.event.{GpioPinDigitalStateChangeEvent, GpioPinListener, GpioPinListenerDigital}

import java.util.concurrent.TimeUnit

var time = 0L

object TestEcho {
  //val pi4j = Pi4J.newAutoContext()
  val gpio: GpioController  = GpioFactory.getInstance()
  val trigger = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, "", PinState.LOW)
  val echo: GpioPinDigitalInput = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00, "")


  echo.addListener(new GpioPinListenerDigital {
    override def handleGpioPinDigitalStateChangeEvent(event: GpioPinDigitalStateChangeEvent): Unit = {
      if (event.getState == PinState.HIGH) {
        time = System.nanoTime()
        println(s"get High rise event. ${time}")
      }else {
        val curTime = System.nanoTime()  // 343 m/s => 34300 cm/s => 34.3 cm/ms
        println(s"get echo resp $curTime - $time = ${(curTime-time).toDouble/1000000*34.3/2}, ${event.getState}, ${event.getEdge}")
      }
    }
  })

  def getDistance()= {
    //output.pulse(3, TimeUnit.SECONDS, DigitalState.HIGH);
    println("send trigger 2")
    trigger.pulse(10, PinState.HIGH, TimeUnit.MILLISECONDS)

  }
}

@main
def TestEchoMain = {
  while(true) {
    TestEcho.getDistance()
    Thread.sleep(1000)
  }
}

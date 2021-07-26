import com.pi4j.io.gpio._
import com.pi4j.io.gpio.event.{GpioPinDigitalStateChangeEvent, GpioPinListener, GpioPinListenerDigital}
import com.pi4j.io.gpio.trigger.GpioCallbackTrigger

import java.util.concurrent.{Callable, TimeUnit}

object TestEchoObj {
  //val pi4j = Pi4J.newAutoContext()
  val gpio: GpioController  = GpioFactory.getInstance()
  val trigger = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "", PinState.LOW)
  val echo: GpioPinDigitalInput = gpio.provisionDigitalInputPin(RaspiPin.GPIO_05, "")

//  echo.addTrigger(new GpioCallbackTrigger( new Callable[Void] {
//    override def call(): Void = {
//      println("triggered")
//      return null
//    }
//  }))

//  echo.addListener(new GpioPinListenerDigital {
//    var time = System.nanoTime()
//    override def handleGpioPinDigitalStateChangeEvent(event: GpioPinDigitalStateChangeEvent): Unit = {
//      if (event.getState == PinState.HIGH) {
//        time = System.nanoTime()
//        println(s"get High rise event. ${time}")
//      }else {
//        val curTime = System.nanoTime()  // 343 m/s => 34300 cm/s => 34.3 cm/ms
//        println(s"get echo resp $curTime - $time = ${(curTime-time).toDouble*34.3/1000000/2}, ${event.getState}, ${event.getEdge}")
//      }
//    }
//  })

  def getDistance()= {
    trigger.high()
    Thread.sleep(0, 10000)
    trigger.low()
    var startTime = 0L
    var endTime = 0L
    while (echo.getState == PinState.LOW) startTime = System.nanoTime()
    while (echo.getState == PinState.HIGH) endTime = System.nanoTime()
    val timeElasped = (endTime-startTime).toDouble/1000000
    val distance = timeElasped* 34.3 /2
    println(s"get distance : ${distance}")
  }
}

@main
def TestEcho = {
  while(true) {
    TestEchoObj.getDistance()
    Thread.sleep(400)
  }
}

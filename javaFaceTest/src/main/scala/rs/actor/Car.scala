package rs.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import com.pi4j.io.gpio.event.{GpioPinDigitalStateChangeEvent, GpioPinListener, GpioPinListenerDigital}
import com.pi4j.io.gpio.trigger.GpioCallbackTrigger
import com.pi4j.io.gpio.{GpioFactory, GpioController, RaspiPin, GpioPinPwmOutput, Pin}
import scala.io.StdIn

enum CarCommand {
  case Forward, Backward, TurnLeft, TurnRight, Stop, Test
}

class Wheel(p1: Pin, p2: Pin) {

  val fv = 50
  val gpio: GpioController  = GpioFactory.getInstance()

  val p1out: GpioPinPwmOutput = gpio.provisionSoftPwmOutputPin(p1)
  val p2out: GpioPinPwmOutput = gpio.provisionSoftPwmOutputPin(p2)

  def forward = {
    p1out.setPwm(fv); p2out.setPwm(0)
  }

  def backward = {
    p1out.setPwm(0); p2out.setPwm(fv)
  }

  def stop = {
    p1out.setPwm(0); p2out.setPwm(0)
  }
}

class Car {
  val fl = new Wheel(RaspiPin.GPIO_07, RaspiPin.GPIO_00)
  val fr = new Wheel(RaspiPin.GPIO_03, RaspiPin.GPIO_02)
  val bl = new Wheel(RaspiPin.GPIO_25, RaspiPin.GPIO_29)
  val br = new Wheel(RaspiPin.GPIO_27, RaspiPin.GPIO_28)


  def forward = { fl.forward ; fr.forward; bl.forward; br.forward }

  def turnLeft = { fl.stop; bl.stop; fr.forward; br.forward}

  def turnRight = { fl.forward; bl.forward; fr.stop; br.stop }

  def backward = { fl.backward; fr.backward; bl.backward; br.backward }

  def stop = { fl.stop; bl.stop; fr.stop; br.stop}

  def test = {
    this.stop
    fl.forward
    StdIn.readLine()
    fl.stop
    fr.forward
    StdIn.readLine()
    fr.stop
    bl.forward
    StdIn.readLine()
    bl.stop
    br.forward
    StdIn.readLine()
    this.stop
  }

  def ready(): Behavior[CarCommand] = Behaviors.receive { (ctx, msg:CarCommand) =>
    msg match {
      case CarCommand.Forward => forward
      case CarCommand.Backward => backward
      case CarCommand.TurnRight => turnRight
      case CarCommand.TurnLeft => turnLeft
      case CarCommand.Stop => stop
      case CarCommand.Test => test
    }
    Behaviors.same
  }
}



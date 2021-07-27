package rs.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import com.pi4j.io.gpio.event.{GpioPinDigitalStateChangeEvent, GpioPinListener, GpioPinListenerDigital}
import com.pi4j.io.gpio.trigger.GpioCallbackTrigger
import com.pi4j.io.gpio.{GpioFactory, GpioController, RaspiPin, GpioPinPwmOutput, Pin}
import scala.io.StdIn
import rs.actor.I2cDev

enum CarCommand {
  case Forward, Backward, TurnLeft, TurnRight, Stop, Test
}

class Wheel(p1: Int, p2: Int) {

  val fv = 30
  val i2c = I2cDev

  def forward = {
    i2c.setPwmRate(p1, fv)
    i2c.setPwmRate(p2, 0)
  }

  def backward = {
    i2c.setPwmRate(p1, 0)
    i2c.setPwmRate(p2, fv)
  }

  def stop = {
    i2c.setPwmRate(p1, 0)
    i2c.setPwmRate(p2, 0)
  }
}

class Car {
  val fr = new Wheel(4, 5)
  val br = new Wheel(6, 7)
  val bl = new Wheel(8, 9)
  val fl = new Wheel(10, 11)


  def forward = { fl.forward ; fr.forward; bl.forward; br.forward }

  def turnLeft = { fl.stop; bl.stop; fr.forward; br.forward}

  def turnRight = { fl.forward; bl.forward; fr.stop; br.stop }

  def backward = { fl.backward; fr.backward; bl.backward; br.backward }

  def stop = { fl.stop; bl.stop; fr.stop; br.stop}

  def test = {
    this.stop
    println("start test front left wheel.")
    fl.forward
    StdIn.readLine()
    fl.stop
    println("start test front right wheel.")
    fr.forward
    StdIn.readLine()
    fr.stop
    println("start test back left wheel.")
    bl.forward
    StdIn.readLine()
    bl.stop
    println("start test back right wheel.")
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



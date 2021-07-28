package rs.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import com.pi4j.io.gpio.event.{GpioPinDigitalStateChangeEvent, GpioPinListener, GpioPinListenerDigital}
import com.pi4j.io.gpio.trigger.GpioCallbackTrigger
import com.pi4j.io.gpio.{GpioFactory, GpioController, RaspiPin, GpioPinPwmOutput, Pin}
import scala.io.StdIn
import rs.actor.I2cDev

sealed trait CarCommand1
case class Forward(velocity: Double) extends CarCommand1
case class Backward(velocity: Double) extends CarCommand1
case class TurnLeft(velocity: Double) extends CarCommand1
case class TurnRight(velocity: Double) extends CarCommand1
case class Stop() extends CarCommand1
case class Test() extends CarCommand1

enum CarCommand {
  case Forward, Backward, TurnLeft, TurnRight, Stop, Test
}

class Wheel(p1: Int, p2: Int) {

  def forward(velocity: Double) = {
    I2cDev.setPwmRate(p1, velocity)
    I2cDev.setPwmRate(p2, 0)
  }

  def backward(velocity: Double) = {
    I2cDev.setPwmRate(p1, 0)
    I2cDev.setPwmRate(p2, velocity)
  }

  def stop = {
    I2cDev.setPwmRate(p1, 0)
    I2cDev.setPwmRate(p2, 0)
  }

  def hold = {
    I2cDev.setPwmRate(p1, 20)
    I2cDev.setPwmRate(p2, 20)
  }
}

class Car {
  val fr = new Wheel(4, 5)
  val br = new Wheel(6, 7)
  val bl = new Wheel(8, 9)
  val fl = new Wheel(10, 11)


  def forward(velocity: Double) = { Array(fl, fr, bl, br).map( _.forward(velocity)) }

  def turnLeft(velocity: Double) = { fl.stop; bl.stop; fr.forward(velocity); br.forward(velocity)}

  def turnRight(velocity: Double) = { fl.forward(velocity); bl.forward(velocity); fr.stop; br.stop }

  def backward(velocity: Double) = { Array(fl, fr, bl, br).map(_.backward(velocity)) }

  def stop = { Array(fl, fr, bl, br).map(_.stop) }

  def test = {
    this.stop
    println("start test wheels.")
    val velocity = 30
    Array(fl, fr, bl, br).map { w =>
      w.forward(velocity)
      StdIn.readLine()
      w.stop
    }
    this.stop
  }

  def ready(): Behavior[CarCommand1] = Behaviors.receive { (ctx, msg:CarCommand1) =>
    msg match {
      case Forward(velocity) => forward(velocity)
      case Backward(velocity) => backward(velocity)
      case TurnLeft(velocity) => turnLeft(velocity)
      case TurnRight(velocity) => turnRight(velocity)
      case x: Stop => stop
      case x: Test => test
    }
    Behaviors.same
  }
}



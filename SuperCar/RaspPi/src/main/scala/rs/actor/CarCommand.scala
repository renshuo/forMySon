package rs.actor

sealed trait CarCommand
case class Forward(velocity: Double) extends CarCommand
case class Backward(velocity: Double) extends CarCommand
case class TurnLeft(velocity: Double) extends CarCommand
case class TurnRight(velocity: Double) extends CarCommand
case class Stop() extends CarCommand
case class Test() extends CarCommand
package rs.actor

sealed trait CarCommand extends BaseCommand
case class Forward(velocity: Double) extends CarCommand
case class Backward(velocity: Double) extends CarCommand
case class TurnLeft(velocity: Double) extends CarCommand
case class TurnRight(velocity: Double) extends CarCommand
case class Stop() extends CarCommand
case class Test() extends CarCommand

case class MoveLeft(velocity: Double) extends CarCommand
case class MoveRight(velocity: Double) extends CarCommand
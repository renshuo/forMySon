package rs.actor

sealed trait EchoEvent extends BaseCommand

case class EchoInfo(distance: Double, degree: Double) extends EchoEvent

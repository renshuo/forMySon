package rs.actor

sealed trait LedCommand extends BaseCommand
case class LedToggle() extends LedCommand
case class LedInit() extends LedCommand
case class LedBlink(freq: Double, times: Int) extends LedCommand

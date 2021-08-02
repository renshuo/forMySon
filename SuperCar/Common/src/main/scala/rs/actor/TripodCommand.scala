package rs.actor


sealed trait TripodCommand extends BaseCommand
case class TripodUpdate(v: Double, h: Double, delay: Int=30) extends TripodCommand
case class TripodInfo(pitching: Double, direction: Double) extends TripodCommand
case class TripodVelocity(pitchingVelocity: Double, directionVelocity: Double) extends TripodCommand

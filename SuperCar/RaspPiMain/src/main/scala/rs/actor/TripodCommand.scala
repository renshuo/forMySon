package rs.actor


sealed trait TripodCommand
case class TripodUpdate(v: Double, h: Double) extends TripodCommand
case class TripodInfo(pitching: Double, direction: Double) extends TripodCommand


import akka.actor.typed.{ActorSystem, ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import rs.actor.TripodPwm
import rs.actor.TripodInfo
import scala.io.StdIn

import com.pi4j.io.gpio.{GpioFactory}


@main
def TestTripod(): Unit = {

  val sys = ActorSystem(TripodPwm().directDo(), "camera")
  try{
    while (true) {
      print("input degree: ")
      val dgree = StdIn.readLine()
      val vs = dgree.split(",").map { _.toInt }
      sys.tell(TripodInfo(vs(0), vs(1)))
    }
  }catch {
    case _ => { GpioFactory.getInstance().shutdown(); println("shutdown gpio") }
  }

}
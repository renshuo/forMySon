package rs.source

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import org.scalatest.*
import org.scalatest.flatspec.*
import org.scalatest.matchers.*
import rs.actor.{BaseCommand, Forward, TripodUpdate}
import rs.source.MqttSub

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.sys.process.*

class MqttSubTest extends AnyFlatSpec with should.Matchers {

  val testkit = ActorTestKit()

  "a mqtt sub" should "get message from a mqtt broker" in {
    val ctl = testkit.createTestProbe[BaseCommand]()
    val mqt = testkit.spawn(MqttSub(), "mqtt")
    mqt.tell("start")
    Thread.sleep(1000)

    "mosquitto_pub -t piTripod -m '{\"TripodUpdate\": { \"v\": 1.0, \"h\": 2.0, \"delay\": 1}}'".!
    ctl.expectMessage(FiniteDuration(5, TimeUnit.SECONDS), TripodUpdate(1.0, 2.0, 1))

    """mosquitto_pub -t piCar -m '{"Forward": { "velocity": 1.0}}'""".!
    ctl.expectMessage(FiniteDuration(5, TimeUnit.SECONDS), Forward(1.0))
  }
}


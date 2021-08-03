package rs.controller


import akka.actor.typed.ActorSystem
import org.scalatest.*
import flatspec.*
import matchers.*
import rs.source.MqttSub

class TestMqttSub extends AnyFlatSpec with should.Matchers {

  "a mqtt sub" should "get message from a mqtt broker" in {
    val system = ActorSystem.create(MqttSub(null), "test")
    system.tell("start")
  }
}


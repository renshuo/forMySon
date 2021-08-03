import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import rs.actor.BaseCommand
import rs.source.MqttSub

@main def TestMqtt1: Unit ={
  try {
    val system = ActorSystem.create(MqttSub(null), "test")
    system.tell("start")
  }catch {
    case x => println(s"${x}")
  }

}
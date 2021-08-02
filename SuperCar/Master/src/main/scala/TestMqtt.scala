import akka.Done
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.Materializer
import akka.stream.alpakka.mqtt.scaladsl.{MqttMessageWithAck, MqttSink, MqttSource}
import akka.stream.alpakka.mqtt.{MqttConnectionSettings, MqttMessage, MqttQoS, MqttSubscriptions}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.util.ByteString
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


val connectionSettings = MqttConnectionSettings(
  "tcp://pi2:1883",
  "test",
  new MemoryPersistence
)

@main def MqttSend: Unit ={

  val system = ActorSystem.create(Behaviors.setup(ctx => Behaviors.receive { (ctx,msg) =>

    given materializer: Materializer = Materializer(ctx)
    given executorContext: ExecutionContext = ctx.executionContext
    val msgs = Array(
      MqttMessage("test", ByteString("asdv1")),
      MqttMessage("test", ByteString("asdv2"))
    )

    val sink: Sink[MqttMessage, Future[Done]] = MqttSink(connectionSettings, MqttQoS.AtLeastOnce)
    Source(msgs).runWith(sink)

    Behaviors.same[Any]
  }), "sys")

  system.tell("start")
}


@main def MqttRead: Unit = {
  val system = ActorSystem.create(Behaviors.setup(ctx => Behaviors.receive { (ctx,msg) =>

    given materializer: Materializer = Materializer(ctx)
    given executorContext: ExecutionContext = ctx.executionContext
    val mqttSource = MqttSource.atLeastOnce(
      connectionSettings.withClientId("MqttSend"),
      MqttSubscriptions(Map("test" -> MqttQoS.AtLeastOnce)),
      bufferSize = 8
    )

    val (subscribed, streamResult) = mqttSource
      .map((msg: MqttMessageWithAck) => println(msg.getClass))
      .toMat(Sink.seq)(Keep.both)
      .run()

    Behaviors.same[Any]
  }), "sys")

  system.tell("start")
}
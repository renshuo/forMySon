package rs.source

import akka.Done
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.http.scaladsl.server.Directives.complete
import akka.stream.Materializer
import akka.stream.alpakka.mqtt.scaladsl.{MqttMessageWithAck, MqttSource}
import akka.stream.alpakka.mqtt.{MqttConnectionSettings, MqttMessage, MqttQoS, MqttSubscriptions}
import akka.stream.scaladsl.{Keep, Sink, Source}
import com.typesafe.scalalogging.Logger
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import rs.actor.{BaseCommand, CarCommand, TripodCommand}

import scala.concurrent.Future

val connectionSettings = MqttConnectionSettings(
  "tcp://localhost:1883",
  "test",
  new MemoryPersistence
)

object MqttSub {

  import rs.controllerKey
  var controllerList = Set[ActorRef[BaseCommand]]()

  def apply(): Behavior[String] = {
    Behaviors.setup{context =>

      val blist = context.spawn(Behaviors.receiveMessage{ (list: Receptionist.Listing) =>
        val si: Set[ActorRef[BaseCommand]] = list.serviceInstances(controllerKey)
        this.controllerList = si
        println(s"update actor A list: ${this.controllerList}")
        Behaviors.same
      }, "ctlList")
      context.system.receptionist ! Receptionist.Subscribe(controllerKey, blist.ref)

      new MqttSub(context)
    }
  }
}

class MqttSub(ctx: ActorContext[String]) extends AbstractBehavior[String](ctx) {

  import MqttSub.controllerList

  val piCar = "piCar"
  val piTripod = "piTripod"

  override def onMessage(msg: String): Behavior[String] = {

    val log = Logger(getClass)

    println("start mqtt sub. ")

    given materializer: Materializer = Materializer(ctx)

    val mqttSource: Source[MqttMessage, Future[Done]] =
      MqttSource.atMostOnce(
        connectionSettings.withClientId(clientId = "source-spec/source"),
        MqttSubscriptions(Map(piCar -> MqttQoS.exactlyOnce, piTripod -> MqttQoS.exactlyOnce)),
        bufferSize = 8
      )

    mqttSource.map{ (msg: MqttMessage) =>
      println(s"get msg from ${msg.topic}")
      val str = msg.payload.utf8String
      msg.topic match {
        case x if x==piCar => {
          val decoded = decode[CarCommand](str)
          decoded match {
            case Left(e) => {
              println(s"get invalid command: ${e}")
            }
            case Right(carCmd) => {
              println(s"send command ${carCmd} to car.")
              controllerList.foreach { (ctl: ActorRef[BaseCommand]) =>
                ctl.tell(carCmd)
              }
            }
          }
        }
        case x if x==piTripod => {
          val decoded = decode[TripodCommand](str)
          decoded match {
            case Left(e) => {
              println(s"get invalid command: ${e}")
            }
            case Right(tripodCmd) => {
              println(s"send command ${tripodCmd} to car.")
              try{
                controllerList.foreach { ctl =>
                  ctl.tell(tripodCmd)
                }
              }catch {
                case x: Exception => println(x)
              }
            }
          }
        }
      }
    }.run()

    Behaviors.same
  }
}

package rs.actor

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Post
import akka.stream.Materializer
import com.typesafe.scalalogging.Logger

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}


object WebClientActor {

  def apply() = {
    Behaviors.setup[BaseCommand]( ctx => new WebClientActor(ctx).ready())
  }
}

class WebClientActor(ctx: ActorContext[BaseCommand]) {
  val log = Logger(getClass)

  given executorContext: ExecutionContext = ctx.executionContext
  given materializer: Materializer = Materializer(ctx)
  given system: ActorSystem[BaseCommand] = ctx.system.asInstanceOf[ActorSystem[BaseCommand]]

  def ready(): Behavior[BaseCommand] = Behaviors.receiveMessage{ (msg: BaseCommand) =>
    println(s"get web event : ${msg}")

    import io.circe.parser._
    import io.circe.generic.auto._
    import io.circe.syntax._
    import io.circe._
    val ip = "192.168.31.242"
    val postReq1 = msg match {
      case carCommand: CarCommand => Post(s"http://${ip}:8010/car", msg.asInstanceOf[CarCommand].asJson.noSpaces)
      case tripodCommand: TripodCommand => Post(s"http://${ip}:8010/tripod", msg.asInstanceOf[TripodCommand].asJson.noSpaces)
      case ledCommand: LedCommand => Post(s"http://${ip}:8010/led", msg.asInstanceOf[LedCommand].asJson.noSpaces)
    }
    Http().singleRequest(postReq1).onComplete {
      case Success(resp) => {
        resp.entity.dataBytes
          .map { line =>
            println(line.utf8String)
          }.run()
        // .runForeach( (x: ByteString) => println(x.utf8String))
      }
      case Failure(exception) => println(s"error: ${exception}")
    }
    Behaviors.same
  }
}

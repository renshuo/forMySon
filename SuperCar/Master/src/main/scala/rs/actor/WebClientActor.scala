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
    Behaviors.setup[CarCommand | TripodCommand]( ctx => new WebClientActor(ctx).ready())
  }
}

class WebClientActor(ctx: ActorContext[CarCommand | TripodCommand]) {
  val log = Logger(getClass)

  given executorContext: ExecutionContext = ctx.executionContext
  given materializer: Materializer = Materializer(ctx)
  given system: ActorSystem[CarCommand | TripodCommand] = ctx.system.asInstanceOf[ActorSystem[CarCommand | TripodCommand]]

  def ready(): Behavior[CarCommand | TripodCommand] = Behaviors.receiveMessage{ (msg: CarCommand | TripodCommand) =>
    println(s"get web event : ${msg}")

    import io.circe.parser._
    import io.circe.generic.auto._
    import io.circe.syntax._
    import io.circe._
    val ip = "192.168.31.242"
    val url:String = if (msg.isInstanceOf[CarCommand]) {
      s"http://${ip}:8010/car"
    } else if (msg.isInstanceOf[TripodCommand]) {
      s"http://${ip}:8010/tripod"
    } else {
      s"http://${ip}:8010/"
    }
    val postReq1 = (msg match {
      case x: CarCommand => {
        Post(url, msg.asInstanceOf[CarCommand].asJson.noSpaces)
      }
      case x: TripodCommand => {
        Post(url, msg.asInstanceOf[TripodCommand].asJson.noSpaces)
      }
      case _ => Post(url, "")
    })
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

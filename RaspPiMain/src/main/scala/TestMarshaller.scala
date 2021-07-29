import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, MessageEntity}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.TimeUnit
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.util.Success
import akka.http.scaladsl.marshalling.{Marshal, PredefinedToResponseMarshallers, ToResponseMarshaller}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import spray.json.DefaultJsonProtocol.DoubleJsonFormat

sealed trait Commnad
case class Start(velocity: Double) extends Commnad

trait JsonSup extends SprayJsonSupport with  DefaultJsonProtocol {
  implicit val itemFormat: RootJsonFormat[Start] = jsonFormat1(Start)
}

class Test extends JsonSup {

  def test() = {
    val resp: Marshal[Start] = Marshal(new Start(1.0))
    println(s"asdf: ${resp}")
  }
}

@main def TestJsonMash = {
  Test().test()

}

@main def TestMash: Unit ={
  given ex: ExecutionContext = ExecutionContext.global

  val str = "1"
  val entFue = Marshal(str).to[MessageEntity]
  entFue.onComplete {
    case Success(v) => println(v)
  }
  val ent = Await.result(entFue, Duration(1, TimeUnit.SECONDS))
  println(s"${ent}, ${ent.getClass}")

  Thread.sleep(1)
}
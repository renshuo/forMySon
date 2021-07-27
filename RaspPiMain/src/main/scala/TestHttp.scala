
import akka.actor.Status.{Failure, Success}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.{Http, HttpExt, ServerBuilder}
import akka.http.scaladsl.server.Directives.{complete, concat, get, post, path, as, pathEnd, pathPrefix}

@main def TestHttpMain = {
  val sys = ActorSystem(MainPiActor(), "Pi")

  val router = path("test") {
    get {
      complete {
        "Welcome1."
      }
    }
  }
  val builder: ServerBuilder = Http()(sys).newServerAt("localhost", 8080)
  val server = builder.bindFlow(Route.toFlow(router)(sys))
//  server.onComplete {
//    case Success(binding) => println(s"server start success. ${binding}")
//    case Failure(e) => { println(s"error ${e}"); System.exit(0) }
//    case _ => println("???")
//  }(sys.executionContext)

}

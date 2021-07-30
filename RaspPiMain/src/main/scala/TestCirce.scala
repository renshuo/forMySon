
import io.circe.parser._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe._
import rs.actor.CarCommand
import rs.actor._

sealed trait CirceT
case class Circe(name: String) extends CirceT
case class Circt(name: String) extends CirceT

sealed trait Foo
case class Bar(xs: String) extends Foo
case class Qux(i: Int, d: String) extends Foo
case class Qux1(age1:Int, name: String) extends Foo

@main def TestCirce = {

  val foo: Foo = Qux(13, "a")

  val json = foo.asJson.noSpaces
  println(s"$json, ${json.getClass}")

  val decodedFoo = decode[Foo](json)
  println(decodedFoo)


  val a:CirceT = Circe("circee")
  val json1 = a.asJson.noSpaces
  println(json1)

  val dea = decode[CirceT](json1)
  println(dea)
}
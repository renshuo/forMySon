package rs


import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import org.scalatest.flatspec.AnyFlatSpec
import rs.actor.*
import org.scalatest.matchers.should.Matchers

sealed trait CirceT
case class Circe(name: String) extends CirceT
case class Circt(name: String) extends CirceT

sealed trait Foo
case class Bar(xs: String) extends Foo
case class Qux(i: Int, d: String) extends Foo
case class Qux1(age1:Int, name: String) extends Foo

class CirceTest extends AnyFlatSpec with Matchers {

  "circe test " should "show hao to use circe" in {
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
}

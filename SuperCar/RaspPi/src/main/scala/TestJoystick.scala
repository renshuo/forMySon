

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import rs.actor.{JoyCommand, JoySticker}

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, DataInputStream, File, FileInputStream}
import java.time.{LocalDateTime, ZoneOffset}

extension (b: Byte) {
  def unsign(bit: Int): Long = (b & 0xff).toLong << bit
}



object Stick {
  val btnNames: Array[String] = Array("Triangle", "Circle", "X", "Square", "Left A", "Right A", "Left B", "Right B", "Select", "Start", "Left Axis", "Right Axis")
  val btns: Array[Boolean] = new Array[Boolean](11)

  var axisX : Long = 0
  var axisY: Long = 0

  override def toString: String = {
    var str = ""
    for (i <- 0 until 10) {
       str += s"${btnNames(i)}: ${ if (btns(i)) 1 else 0}, "
    }
    str
  }
}

@main def TestJoy2 = {
  val sys = ActorSystem.create(JoySticker(), "sys")
  val joyCmdHandler = sys.systemActorOf(Behaviors.receive[JoyCommand] { (ctx, msg) =>
    println(s"get msg ${msg}")
    Behaviors.same
  }, "joyHandler")
  sys.tell(joyCmdHandler)
}

@main def TestJoy: Unit ={
  val f:File = File("/dev/input/js0")
  val os = new FileInputStream(f)
  while(true) {
    val ev = os.readNBytes(8)
    val timeStamp = ev(0).unsign(0) + ev(1).unsign(8) + ev(2).unsign(16) + ev(3).unsign(24)
    val value = ev(4).toLong + ev(5).toLong << 8
    val type1 = ev(6).unsign(0)
    val num = ev(7).unsign(0)

    type1 match {
      case 1 => {
        Stick.btns(num.toInt) = value==256
        printf(s"time: %d.%3d ${Stick} $value\n", timeStamp/1000, timeStamp%1000)
      }
      case 2 => {
        num match {

          case 0 => {Stick.axisX = value; printf(s"time: %d.%3d ${Stick} $value\n", timeStamp/1000, timeStamp%1000) }
          case 1 => {Stick.axisY = value; printf(s"time: %d.%3d ${Stick} $value\n", timeStamp/1000, timeStamp%1000) }
          case _ => {} //printf(s"time: %d.%3d  $value $type1 $num \n", timeStamp/1000, timeStamp%1000)
        }
      }
      case _ => printf(s"time: %d.%3d  $value $type1 $num \n", timeStamp/1000, timeStamp%1000)
    }
  }


}
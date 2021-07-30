package rs.actor

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import com.typesafe.scalalogging.Logger

import java.io.{File, FileInputStream}
import java.nio.file.Paths
import scala.concurrent.{ExecutionContext, Future}

sealed trait JoyCommand
case class JoyBtnEvent(btnNum: Int, isDown: Boolean) extends JoyCommand
case class JoyAxisEvent(axisNum: Int, axisValue: Int) extends JoyCommand

object JoySticker {
  def apply() = {
    Behaviors.setup[ActorRef[JoyCommand]]( ctx => new JoySticker(ctx).start())
  }
}

class JoySticker(ctx: ActorContext[ActorRef[JoyCommand]]) {

  val log = Logger(getClass)

  val f:File = File("/dev/input/js0")
  val os = new FileInputStream(f)

  extension (b: Byte) {
    def unsign(bit: Int): Long = (b & 0xff).toLong << bit
  }

  def start():Behavior[ActorRef[JoyCommand]] = Behaviors.receiveMessage { (joyEventHandler: ActorRef[JoyCommand]) =>
    /**
     * 如果使用akka stream, 底层nio在读取文件前会执行seek操作，但是 js0 是不支持seek的
     */
    println("start joy")
    given system: ActorSystem[Nothing] = ctx.system
    given ec : ExecutionContext = ctx.executionContext

    Future {
      while(true) {
        val ev = os.readNBytes(8)
        val timeStamp = ev(0).unsign(0) + ev(1).unsign(8) + ev(2).unsign(16) + ev(3).unsign(24)
        val value = ev(4).toLong + ev(5).toLong << 8
        val type1 = ev(6).unsign(0)
        val num = ev(7).unsign(0)

        type1 match {
          case 1 => {
            if (value==256)
              joyEventHandler.tell(JoyBtnEvent(num.toInt, true))
            else
              joyEventHandler.tell(JoyBtnEvent(num.toInt, false))
          }
          case 2 => {
            joyEventHandler.tell(JoyAxisEvent(num.toInt, value.toInt))
          }
          case _ => printf(s"time: %d.%3d  $value $type1 $num \n", timeStamp/1000, timeStamp%1000)
        }
      }
    }
    Behaviors.same
  }
}

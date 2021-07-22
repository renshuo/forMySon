import javafx.embed.swing.SwingFXUtils
import javafx.event.ActionEvent
import javafx.scene.image.WritableImage
import scalafx.scene.media.{Media, MediaPlayer, MediaView}
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.effect.DropShadow
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.paint.Color._
import scalafx.scene.paint._
import scalafx.scene.text.Text
import javafx.scene.input.{KeyCode, KeyEvent, MouseEvent}
import scalafx.scene.media.MediaPlayer
import scalafx.scene.media.MediaView
import scalafx.scene.media._

import scalafx.Includes._

import java.io.File
import java.nio.file.{Files, Paths}
import javax.imageio.ImageIO


def readVideo(): MediaView = {
  val res = Paths.get("/home/work/test.mp4").toUri.toString
  val media = new Media(res)
  val player = new MediaPlayer(media)

  val w = media.getWidth()
  val h = media.getHeight()

  println(s"media: $w $h $player")



  player.setAutoPlay(true)

  val mv = new MediaView(player)
  mv.setFitWidth(960)
  mv.setFitHeight(540)
  for ( i<- 0 to 100) {


  }
  mv
}

var count = 0

def snapshot1(view: MediaView):Unit = {
  count += 1
  val image = new WritableImage(960, 540)
  view.snapshot(null, image)
  ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", new File(s"/home/work/test${count}.png"))
}

object VideoInputFx extends JFXApp3 {
//  val media = new Media("/home/work/test.mp4")
//  val player = new MediaPlayer(media)
//  val mview = new MediaView(player)



  override def start(): Unit = {
    stage = new JFXApp3.PrimaryStage {
      title = "主界面"
      scene = new Scene {
        content = new BorderPane {
          minWidth = 800
          minHeight = 600
          style = "-fx-font: normal bold 10pt 'Source Han Sans CN'"
          onKeyPressed = { e =>
            if e.getCode==KeyCode.ESCAPE then System.exit(0)
          }
          val mv = readVideo()
          left = mv
          right = new VBox {
            children = Seq(
              new Text { text = "abc"},
              new Button("snap") {
                onAction =  (e: ActionEvent) => {
                  println("btn1")
                  snapshot1(mv)
                }
              }

            )
          }
          bottom = new HBox {
            children = Seq(
              new Label { text = "abc"}
            )
          }
        }
      }
    }
  }

}

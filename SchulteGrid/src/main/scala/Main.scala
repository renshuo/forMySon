import javafx.application.Application
import javafx.beans.property.SimpleStringProperty
import org.apache.poi.ss.usermodel.{BorderStyle, HorizontalAlignment, VerticalAlignment}
import org.apache.poi.xssf.usermodel.{XSSFCellStyle, XSSFWorkbook}
import scalafx.application.JFXApp3
import scalafx.beans.property.StringProperty
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.VBox
import scalafx.scene.paint.Color.LightGreen
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.Text
import scalafx.stage.Stage

import java.io.{File, FileOutputStream}
import java.util
import javax.swing.GroupLayout.Alignment
import scala.util.Random


def msg = "I was compiled by Scala 3. :)"

object Main extends JFXApp3 {
  override def main(args: Array[String]): Unit = super.main(args)

  def generateShult() = {
    val res = scala.collection.mutable.ListBuffer[Int]()
    while (res.size < 9) {
      val v = Random.nextInt(9) + 1
      if (!res.contains(v)) {
        res.addOne(v)
      }
    }
    res
  }
  
  def writeToExcel():Unit = {
    val xls:XSSFWorkbook = new XSSFWorkbook()
    val sheet = xls.createSheet()
    sheet.setFitToPage(false)
    sheet.setDefaultColumnWidth(6)
    sheet.setDefaultRowHeight(600)
    val fos = new FileOutputStream(new File("./res.xls"))
    val pageNum = 5
    
    for (rownum <- 1 to (6*(3+1)+1) * pageNum){
      val row  = sheet.createRow(rownum)
      row.setHeight(600)
    } 
    for (p <- 0 until pageNum) {
      for (c <- 0 until 3) {
        for (r <- 0 until 6) {
          val a = generateShult()
          for (i <- 0 until 3) {
            val row = sheet.getRow(p*24 + r * 4 + i + 1)
            for (j <- 0 until 3) {
              val cell = row.createCell(c * 4 + j)
              val v = a(i * 3 + j)
              cell.setCellValue(v)
              val style = xls.createCellStyle()
              style.setBorderBottom(BorderStyle.THIN)
              style.setBorderTop(BorderStyle.THIN)
              style.setBorderLeft(BorderStyle.THIN)
              style.setBorderRight(BorderStyle.THIN)
              style.setAlignment(HorizontalAlignment.CENTER)
              style.setVerticalAlignment(VerticalAlignment.CENTER)
              cell.setCellStyle(style)
            }
          }
        }
        sheet.setColumnWidth(c * 4 + 3, 256 * 8)
      }
      //sheet.setRowBreak((p+1)*25)
    }

    xls.write(fos)
    fos.close()
    xls.close()
  }

    val a = new SimpleStringProperty("")
    
  def start(): Unit = {
    stage = new JFXApp3.PrimaryStage {
      title.value = "Hello Stage"
      width = 600
      height = 450
      scene = new Scene {
        content = new VBox(
          new Button("start") {
            onMouseClicked = (ev) => {
              println("mouse clicked.")
              val seq = generateShult()
              writeToExcel()
            }
          },
            new Text() {
                this.textProperty().bindBidirectional(a)
            }
        )
      }

    }
  }
}




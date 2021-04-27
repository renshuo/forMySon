import javafx.application.Application
import javafx.beans.property.SimpleStringProperty
import org.apache.poi.ss.usermodel.{BorderStyle, HorizontalAlignment, VerticalAlignment}
import org.apache.poi.xssf.usermodel.{XSSFCellStyle, XSSFWorkbook}
import org.apache.poi.xwpf.usermodel.XWPFTableCell.XWPFVertAlign
import org.apache.poi.xwpf.usermodel.{ParagraphAlignment, XWPFDocument, XWPFParagraph, XWPFTableCell}
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPageMargins
import org.openxmlformats.schemas.wordprocessingml.x2006.main.{CTPageMar, CTSectPr, CTTblWidth, CTTc, STBorder}
import scalafx.application.JFXApp3
import scalafx.beans.property.StringProperty
import scalafx.geometry.Pos
import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.layout.{BorderPane, HBox, VBox}
import scalafx.scene.paint.Color.LightGreen
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.Text
import scalafx.stage.Stage

import java.io.{File, FileOutputStream}
import java.util
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

  def setOutterCellStyle(cell: XWPFTableCell): Unit = {
    val tc: CTTc = cell.getCTTc()
    val pr = tc.addNewTcPr()
    val border = pr.addNewTcBorders()
    border.addNewTop().setVal(STBorder.NIL)
    border.addNewBottom().setVal(STBorder.NIL)
    border.addNewLeft().setVal(STBorder.NIL)
    border.addNewRight().setVal(STBorder.NIL)
    val margin = pr.addNewTcMar()
    margin.addNewLeft().setW(500)
    margin.addNewRight().setW(500)
    margin.addNewTop().setW(280)
    margin.addNewBottom().setW(280)
  }
  
  def setInnerCellStyle(cell: XWPFTableCell): Unit = {
    cell.setVerticalAlignment(XWPFVertAlign.CENTER)
    cell.setWidth(s"${256 * 3}")
    cell.getParagraphs().get(0).setAlignment(ParagraphAlignment.CENTER)
    val border = cell.getCTTc().getTcPr().addNewTcBorders()
    border.addNewTop().setVal(STBorder.SINGLE)
    border.addNewBottom().setVal(STBorder.SINGLE)
    border.addNewLeft().setVal(STBorder.SINGLE)
    border.addNewRight().setVal(STBorder.SINGLE)
  }
  
  def writeToWord() = {
    val doc = new XWPFDocument()

    val pr1: CTSectPr = doc.getDocument().getBody().addNewSectPr()
    val mgr: CTPageMar = pr1.addNewPgMar()
    mgr.setLeft(1800) // default 1800L
    mgr.setRight(1800)
    mgr.setTop(400)
    mgr.setBottom(400)
    
    
    for (pageNum <- 0 until 5) {
      val p = doc.createParagraph()
      val tb = doc.createTable(6, 3)
      tb.getRows.forEach { row =>
        row.getTableCells().forEach { cell =>
          setOutterCellStyle(cell)
          val a = generateShult()
          val tb = cell.insertNewTbl(cell.getCTTc().getTcPr().newCursor())
          for (i <- 0 until 3) {
            val xrow = tb.createRow()
            tb.addRow(xrow, i)
            xrow.setHeight(600)
            for (j <- 0 until 3) {
              val cell = xrow.addNewTableCell()
              cell.setText("" + (a(i * 3 + j)))
              setInnerCellStyle(cell)
            }
          }
        }
      }
      p.setPageBreak(true)
    }
    val fos: FileOutputStream = new FileOutputStream(new File(fileName.get()))
    doc.write(fos)
    doc.close()
    fos.close()
  }

  def writeToExcel(): Unit = {
    val xls: XSSFWorkbook = new XSSFWorkbook()
    val sheet = xls.createSheet()
    sheet.setFitToPage(false)
    sheet.setDefaultColumnWidth(6)
    sheet.setDefaultRowHeight(600)
    val fos = new FileOutputStream(new File("./res.xls"))
    val pageNum = 5

    for (rownum <- 1 to (6 * (3 + 1) + 1) * pageNum) {
      val row = sheet.createRow(rownum)
      row.setHeight(600)
    }
    for (p <- 0 until pageNum) {
      for (c <- 0 until 3) {
        for (r <- 0 until 6) {
          val a = generateShult()
          for (i <- 0 until 3) {
            val row = sheet.getRow(p * 24 + r * 4 + i + 1)
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

  val fileName = new SimpleStringProperty("./a.doc")

  def start(): Unit = {
    stage = new JFXApp3.PrimaryStage {
      title.value = "Hello Stage"
      width = 600
      height = 450
      scene = new Scene {
        content = new BorderPane(){
          top = new HBox(
            new Label("上边距："),
            new TextField(){text="200"}
          ){
            this.setAlignment(Pos.BaselineCenter)
          }
          left = new VBox(
            new Label("左边距："),
            new TextField(){text="200"; minWidth(120)},
            new TextField() {
              text.bindBidirectional(fileName)
            },
            new Button("Create Excel") {
              onMouseClicked = (ev) => {
                writeToExcel()
              }
            },
            new Button("Create Doc") {
              onMouseClicked = (ev) => {
                writeToWord()
              }
            },
            new Text() {
              this.textProperty().bindBidirectional(fileName)
            }
          )
          right = new VBox(
            new Label("右边距："),
            new TextField(){text="300"; minWidth(120)}
          )
          bottom = new HBox(
            new Label("下边距："),
            new TextField(){text="400"}
          ){
            this.setAlignment(Pos.BaselineCenter)
          }
          
          center = new Canvas(){
            this.width = 600
            this.height = 400
          }
        }
      }
    }
  }
}




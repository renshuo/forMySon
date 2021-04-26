val scala3Version = "3.0.0-RC1"


lazy val root = project
  .in(file("."))
  .settings(
      name := "scala3-simple",
      version := "0.1.0",
      scalaVersion := scala3Version,
      libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test
  )


lazy val osName = System.getProperty("os.name") match {
    case n if n.startsWith("Linux")   => "linux"
    case n if n.startsWith("Mac")     => "mac"
    case n if n.startsWith("Windows") => "win"
    case _ => throw new Exception("Unknown platform!")
}

lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
libraryDependencies ++= javaFXModules.map( m =>
    "org.openjfx" % s"javafx-$m" % "15.0.1" classifier osName
)

libraryDependencies += "org.scalafx" % "scalafx_2.13" % "15.0.1-R21" 

libraryDependencies += "org.apache.poi" % "poi-ooxml" % "5.0.0"
libraryDependencies += "org.apache.poi" % "poi-ooxml-full" % "5.0.0"
libraryDependencies += "org.apache.poi" % "poi-ooxml-schemas" % "4.1.2"
libraryDependencies += "org.apache.poi" % "poi-scratchpad" % "5.0.0"


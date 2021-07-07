name := "faceTest"
version := "0.1"

scalaVersion := "3.0.0"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature")


libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test

/* add scalafx dep */
libraryDependencies += "org.scalafx" % "scalafx_3" % "16.0.0-R24"
libraryDependencies ++= {
  lazy val osName = System.getProperty("os.name") match {
    case n if n.startsWith("Linux") => "linux"
    case n if n.startsWith("Mac") => "mac"
    case n if n.startsWith("Windows") => "win"
    case _ => throw new Exception("Unknown platform!")
  }
  Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
    .map(m => "org.openjfx" % s"javafx-$m" % "16" classifier osName)
}

/* scalaTest */
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.9"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test"

/* javacv */
libraryDependencies += "org.bytedeco" % "javacv-platform" % "1.5.5"

// Fork a new JVM for 'run' and 'test:run', to avoid JavaFX double initialization problems
fork := true

mainClass := Some("sren.facetest.TestJavacv")

assembly / assemblyMergeStrategy  := {
  case PathList("javafx", "module-info.class") => MergeStrategy.discard
  case PathList("scalactic") => MergeStrategy.discard
  case PathList("scalatest") => MergeStrategy.discard
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case x => MergeStrategy.first
}

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
libraryDependencies += "org.bytedeco" % "javacv" % "1.5.5"
libraryDependencies += "org.bytedeco" % "ffmpeg" % "4.3.2-1.5.5" classifier "linux-x86_64"

/* pi4j */
libraryDependencies += "com.pi4j" % "pi4j-core" % "1.4"
//libraryDependencies += "com.pi4j" % "pi4j-device" % "1.4"
//libraryDependencies += "com.pi4j" % "pi4j-gpio-extension" % "1.4"
//libraryDependencies += "com.pi4j" % "pi4j-service" % "1.4"

// Fork a new JVM for 'run' and 'test:run', to avoid JavaFX double initialization problems
fork := true

mainClass := Some("sren.facetest.TestJavacv")

assembly / assemblyMergeStrategy  := {
  case PathList("javafx", "module-info.class") => MergeStrategy.discard
  case PathList("scalactic") => MergeStrategy.discard
  case PathList("scalatest") => MergeStrategy.discard
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case m if m.toLowerCase.contains("android-arm64") => MergeStrategy.discard
  case m if m.toLowerCase.contains("android-arm") => MergeStrategy.discard
  case m if m.toLowerCase.contains("android-x86") => MergeStrategy.discard
  case m if m.toLowerCase.contains("android-x86_64") => MergeStrategy.discard
  case m if m.toLowerCase.contains("linux-arm64") => MergeStrategy.discard
  case m if m.toLowerCase.contains("linux-armhf") => MergeStrategy.discard
  case m if m.toLowerCase.contains("linux-ppc64le") => MergeStrategy.discard
  case m if m.toLowerCase.contains("linux-x86") => MergeStrategy.discard
  case m if m.toLowerCase.contains("macosx-x86_64") => MergeStrategy.discard
  case m if m.toLowerCase.contains("windows-x86") => MergeStrategy.discard
  case m if m.toLowerCase.contains("windows-x86_64") => MergeStrategy.discard

  case x => MergeStrategy.first
}

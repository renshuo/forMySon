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
  Seq("base", "controls", "fxml", "graphics", "media", "swing"
  //  , "web"
  )
    .map(m => "org.openjfx" % s"javafx-$m" % "16" classifier osName)
}

/* scalaTest */
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.9"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test"

/* javacv */
val javacvClassifier = "linux-x86_64"
//libraryDependencies += "org.bytedeco" % "javacv-platform" % "1.5.5"
libraryDependencies += "org.bytedeco" % "javacv" % "1.5.5"
//libraryDependencies += "org.bytedeco" % "artoolkitplus" % "2.3.1-1.5.5" classifier javacvClassifier
libraryDependencies += "org.bytedeco" % "ffmpeg" % "4.3.2-1.5.5" classifier javacvClassifier
//libraryDependencies += "org.bytedeco" % "flandmark" % "1.07-1.5.5" classifier javacvClassifier
//libraryDependencies += "org.bytedeco" % "flycapture" % "2.13.3.31-1.5.5" classifier javacvClassifier
libraryDependencies += "org.bytedeco" % "javacpp" % "1.5.5" classifier javacvClassifier
//libraryDependencies += "org.bytedeco" % "leptonica" % "1.80.0-1.5.5" classifier javacvClassifier
//libraryDependencies += "org.bytedeco" % "libdc1394" % "2.2.6-1.5.5" classifier javacvClassifier
//libraryDependencies += "org.bytedeco" % "libfreenect" % "0.5.7-1.5.5" classifier javacvClassifier
//libraryDependencies += "org.bytedeco" % "librealsense2" % "2.40.0-1.5.5" classifier javacvClassifier
//libraryDependencies += "org.bytedeco" % "librealsense" % "1.12.4-1.5.5" classifier javacvClassifier
libraryDependencies += "org.bytedeco" % "openblas" % "0.3.13-1.5.5" classifier javacvClassifier
libraryDependencies += "org.bytedeco" % "opencv" % "4.5.1-1.5.5" classifier javacvClassifier
//libraryDependencies += "org.bytedeco" % "tesseract" % "4.1.1-1.5.5" classifier javacvClassifier
//libraryDependencies += "org.bytedeco" % "videoinput" % "0.200-1.5.5"

/* pi4j */
libraryDependencies += "com.pi4j" % "pi4j-core" % "1.4"
//libraryDependencies += "com.pi4j" % "pi4j-device" % "1.4"
//libraryDependencies += "com.pi4j" % "pi4j-gpio-extension" % "1.4"
//libraryDependencies += "com.pi4j" % "pi4j-service" % "1.4"

/* akka */
libraryDependencies += "com.typesafe.akka" % "akka-actor-typed_2.13" % "2.6.15"
// libraryDependencies += "com.typesafe.akka" % "akka-cluster-typed_2.13" % "2.6.15"

/* log */
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
// Fork a new JVM for 'run' and 'test:run', to avoid JavaFX double initialization problems

libraryDependencies += "ai.djl" % "api" % "0.12.0"
libraryDependencies += "ai.djl" % "model-zoo" % "0.12.0"

//libraryDependencies += "ai.djl.mxnet" % "mxnet-engine" % "0.12.0"
//libraryDependencies += "ai.djl.mxnet" % "mxnet-model-zoo" % "0.12.0"
//libraryDependencies += "ai.djl.mxnet" % "mxnet-native-auto" % "1.8.0"
libraryDependencies += "ai.djl.pytorch" % "pytorch-engine" % "0.12.0"
libraryDependencies += "ai.djl.pytorch" % "pytorch-model-zoo" % "0.12.0"
//libraryDependencies += "ai.djl.pytorch" % "pytorch-native-auto" % "1.8.1"
libraryDependencies += "ai.djl.pytorch" % "pytorch-native-cpu" % "1.8.1" classifier "linux-x86_64"
//libraryDependencies += "ai.djl.tensorflow" % "tensorflow-native-auto" % "2.4.1"



fork := true

mainClass := Some("MainAkka")

assembly / assemblyMergeStrategy  := {
  case PathList("javafx", "module-info.class") => MergeStrategy.discard
  case PathList("scalactic") => MergeStrategy.discard
  case PathList("scalatest") => MergeStrategy.discard
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case m if m.toLowerCase.contains("android") => MergeStrategy.discard
  case m if m.toLowerCase.contains("windows") => MergeStrategy.discard
  case m if m.toLowerCase.contains("ios") => MergeStrategy.discard

  //case m if m.toLowerCase.contains("x86") => MergeStrategy.discard
  case m if m.toLowerCase.contains("ppc") => MergeStrategy.discard
  case m if m.toLowerCase.contains("arm64") => MergeStrategy.discard

  /*
   * fix error: No configuration setting found for key 'akka.loggers'
   * see https://stackoverflow.com/questions/31011243/no-configuration-setting-found-for-key-akka-version
   */
  case "reference.conf" => MergeStrategy.concat

  case m if m.toLowerCase.contains("torch") => MergeStrategy.discard
  //case m if m.toLowerCase.contains("bytedeco") => MergeStrategy.discard

  case x => MergeStrategy.first
}

import sbt.Keys.runBefore

name := "RaspPi"

scalacOptions ++= Seq("-unchecked","-encoding", "utf8", "-feature")

/* scalaTest */
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.9"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test"

/* pi4j */
libraryDependencies += "com.pi4j" % "pi4j-core" % "1.4"
//libraryDependencies += "com.pi4j" % "pi4j-device" % "1.4"
//libraryDependencies += "com.pi4j" % "pi4j-gpio-extension" % "1.4"
//libraryDependencies += "com.pi4j" % "pi4j-service" % "1.4"

/* akka */
libraryDependencies += "com.typesafe.akka" % "akka-actor-typed_2.13" % "2.6.15"
libraryDependencies += "com.typesafe.akka" % "akka-stream_2.13" % "2.6.15"
libraryDependencies += "com.typesafe.akka" % "akka-http_2.13" % "10.2.5-M2"
//libraryDependencies += "com.typesafe.akka" % "akka-http-spray-json_2.13" % "10.2.5-M2"
libraryDependencies += "io.circe" %% "circe-core" % "0.14.1"
libraryDependencies += "io.circe" %% "circe-generic" % "0.14.1"
libraryDependencies += "io.circe" %% "circe-parser" % "0.14.1"


/* log */
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"

mainClass := Some("MainPi")

assembly / assemblyJarName := "raspMain.jar"

assembly / assemblyMergeStrategy  := {
  case x if Assembly.isConfigFile(x) =>
    MergeStrategy.concat
  case PathList(ps @ _*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
    MergeStrategy.rename
  case PathList("META-INF", xs @ _*) =>
    (xs map {_.toLowerCase}) match {
      case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
        MergeStrategy.discard
      case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
        MergeStrategy.discard
      case "plexus" :: xs =>
        MergeStrategy.discard
      case "services" :: xs =>
        MergeStrategy.filterDistinctLines
      case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
        MergeStrategy.filterDistinctLines
      case _ => MergeStrategy.deduplicate
    }

  /*
   * fix error: No configuration setting found for key 'akka.loggers'
   * see https://stackoverflow.com/questions/31011243/no-configuration-setting-found-for-key-akka-version
   */
  case "reference.conf" => MergeStrategy.concat
  case _ => MergeStrategy.deduplicate
}
//import deployssh.DeploySSH._
//lazy val myProject = project.enablePlugins(DeploySSH)

val upload = taskKey[Unit]("")
upload:= {
  import sys.process._
  "scp ./RaspPi/target/scala-3.0.1/raspMain.jar pi2:".!
  println("upload success.")
}


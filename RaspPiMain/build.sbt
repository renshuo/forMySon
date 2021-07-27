name := "RaspPiMain"
version := "0.1"

scalaVersion := "3.0.0"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature")

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
// libraryDependencies += "com.typesafe.akka" % "akka-cluster-typed_2.13" % "2.6.15"

/* log */
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

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

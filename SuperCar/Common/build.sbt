name := "Common"
version := "0.1"

scalaVersion := "3.0.1"

scalacOptions ++= Seq("-unchecked","-encoding", "utf8", "-feature")

/* scalaTest */
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.9"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test"

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

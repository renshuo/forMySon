name := "superCar"
version := "0.2"

scalaVersion := "3.0.1"

scalacOptions ++= Seq("-unchecked","-encoding", "utf8", "-feature")


lazy val Common = project

lazy val RaspPiMain = project


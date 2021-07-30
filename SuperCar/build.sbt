name := "superCar"
version := "0.2"

scalaVersion := "3.0.1"

scalacOptions ++= Seq("-unchecked","-encoding", "utf8", "-feature")

lazy val root = (project in file("."))


lazy val Common = project

lazy val RaspPi = project.dependsOn(Common)

lazy val Master = project.dependsOn(Common)

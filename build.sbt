name := "scamper"
version := "0.4.0-SNAPSHOT"
organization := "losizm.scamper"

scalaVersion := "2.12.4"
scalacOptions := Seq("-deprecation", "-feature", "-Xcheckinit")

libraryDependencies := Seq(
  "com.typesafe"  %  "config"    % "1.3.2",
  "losizm.bantam" %% "bantam-nx" % "1.0.0",
  "org.scalatest" %% "scalatest" % "3.0.4" % "test"
)


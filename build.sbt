organization := "com.github.losizm"
name         := "scamper"
version      := "40.0.8"
description  := "The HTTP library for Scala"
homepage     := Some(url("https://github.com/losizm/scamper"))
licenses     := List("Apache License, Version 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

scalaVersion := "3.3.1"
scalacOptions := Seq("-deprecation", "-feature", "-new-syntax", "-Werror", "-Yno-experimental")

versionScheme := Some("early-semver")

Compile / doc / scalacOptions := Seq(
  "-project", name.value.capitalize,
  "-project-version", version.value,
  "-project-logo", "images/logo.svg"
)

libraryDependencies ++= Seq(
  "org.slf4j"      %  "slf4j-api"          % "2.0.9",
  "ch.qos.logback" %  "logback-classic"    % "1.4.11"  % Test,
  "org.scalatest"  %% "scalatest-flatspec" % "3.2.17"  % Test
)

scmInfo := Some(
  ScmInfo(
    url("https://github.com/losizm/scamper"),
    "scm:git@github.com:losizm/scamper.git"
  )
)

developers := List(
  Developer(
    id    = "losizm",
    name  = "Carlos Conyers",
    email = "carlos.conyers@hotmail.com",
    url   = url("https://github.com/losizm")
  )
)

publishMavenStyle := true

pomIncludeRepository := { _ => false }

publishTo := {
  val nexus = "https://oss.sonatype.org"
  isSnapshot.value match {
    case true  => Some("snaphsots" at s"$nexus/content/repositories/snapshots")
    case false => Some("releases"  at s"$nexus/service/local/staging/deploy/maven2")
  }
}

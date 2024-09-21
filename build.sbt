organization := "com.github.losizm"
name         := "scamper"
version      := "41.0.0-SNAPSHOT"
description  := "The HTTP library for Scala"
homepage     := Some(url("https://github.com/losizm/scamper"))
licenses     := List("Apache License, Version 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

scalaVersion := "3.3.3"
scalacOptions := Seq("-deprecation", "-feature", "-new-syntax", "-Werror", "-Yno-experimental")

versionScheme := Some("early-semver")

Compile / compile := {
  import java.nio.file.{ Files, Paths }
  import java.util.Properties

  val props = new Properties()
  props.setProperty("product.name", "Scamper")
  props.setProperty("product.version", version.value)

  val dir = (Compile / classDirectory).value
  val out = Files.newBufferedWriter(Paths.get(s"$dir/product.properties"))

  try props.store(out, "Product Properties")
  finally out.close()

  (Compile / compile).value
}

Compile / doc / scalacOptions := Seq(
  "-project", name.value.capitalize,
  "-project-version", version.value,
  "-project-logo", "images/logo.svg"
)

libraryDependencies ++= Seq(
  "org.slf4j"      %  "slf4j-api"          % "2.0.16",
  "ch.qos.logback" %  "logback-classic"    % "1.5.8"  % Test,
  "org.scalatest"  %% "scalatest-flatspec" % "3.2.19" % Test
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

organization := "com.github.losizm"
name         := "scamper"
version      := "21.1.0-SNAPSHOT"
description  := "The HTTP library for Scala"
homepage     := Some(url("https://github.com/losizm/scamper"))
licenses     := List("Apache License, Version 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

scalaVersion := "2.13.5"
scalacOptions ++= Seq("-deprecation", "-feature", "-Xcheckinit")

Compile / doc / scalacOptions ++= Seq(
  "-doc-title",        name.value,
  "-doc-version",      version.value,
  "-doc-root-content", "src/main/scala/root.scala"
)

crossScalaVersions := Seq("2.12.13")

Compile / unmanagedSourceDirectories += {
  (Compile / sourceDirectory).value / s"scala-${scalaBinaryVersion.value}"
}

libraryDependencies += "org.scalatest" %% "scalatest-flatspec" % "3.2.5" % "test"

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

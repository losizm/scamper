organization := "com.github.losizm"
name := "scamper"
version := "9.2.5"

scalaVersion := "2.13.0"
scalacOptions ++= Seq("-deprecation", "-feature", "-Xcheckinit")

crossScalaVersions := Seq("2.12.8")

unmanagedSourceDirectories in Compile += {
  val sourceDir = (sourceDirectory in Compile).value
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 13)) => sourceDir / "scala-2.13"
    case Some((2, 12)) => sourceDir / "scala-2.12"
    case _ => throw new Exception("Scala version must be either 2.12 or 2.13")
  }
}

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"

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

description := "The HTTP library for Scala"
licenses := List("Apache License, Version 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
homepage := Some(url("https://github.com/losizm/scamper"))

pomIncludeRepository := { _ => false }

publishTo := {
  val nexus = "https://oss.sonatype.org"
  if (isSnapshot.value) Some("snaphsots" at s"$nexus/content/repositories/snapshots")
  else Some("releases" at s"$nexus/service/local/staging/deploy/maven2")
}

publishMavenStyle := true


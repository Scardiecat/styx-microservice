import NativePackagerHelper._


val commonSettings = Seq(
  organization := "org.scrdiecat",
  version := "0.0.2-SNAPSHOT",
  scalaVersion := "2.11.7",

  // build info
  buildInfoPackage := "meta",
  buildInfoOptions += BuildInfoOption.ToJson,
  buildInfoKeys := Seq[BuildInfoKey](
    name, version, scalaVersion,
    "sbtNativePackager" -> "1.0.0"
  )
)

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin, JavaAppPackaging)
  .settings(
    name := """styx-microservice""",
    publishMavenStyle := false,
    libraryDependencies ++= Dependencies.spray,
    commonSettings
  )


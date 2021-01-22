resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

val logDeps =
  Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
    "ch.qos.logback" % "logback-classic" % "1.2.3"
  )

lazy val root = (project in file(".")).settings(
  inThisBuild(
    List(
      organization := "be.botkop",
      scalaVersion := "2.12.12",
      version := "0.1.2-SNAPSHOT"
    )
  ),
  name := "scorch",
  libraryDependencies ++= Seq(
    "org.nd4j" % "nd4j-native-platform" % "0.9.1",
    "org.scalatest" %% "scalatest" % "3.0.3"
  ) ++ logDeps
)

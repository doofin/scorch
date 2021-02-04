resolvers ++=
  Seq(
    "jitpack" at "https://jitpack.io"
//    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  )
//https://www.scala-sbt.org/1.x/docs/In-Process-Classloaders.html
classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat

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
    "org.scalatest" %% "scalatest" % "3.0.3",
//    "com.github.doofin" %% "stdscala" % "master-SNAPSHOT"
//    "com.github.doofin" %% "stdScala" % "master-SNAPSHOT"
    "com.doofin" %% "stdscala" % "0.1-SNAPSHOT",
    "org.platanios" %% "tensorflow" % "0.4.1" classifier "linux-cpu-x86_64"
//    "org.deeplearning4j" % "deeplearning4j-core" % "0.9.1"
  ) ++ logDeps ++ (CrossVersion
    .partialVersion(scalaVersion.value) match {
    case Some((2, major)) if major <= 12 =>
      Seq()
    case _ =>
      Seq(
        "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.0"
      )
  })
)

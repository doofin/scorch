resolvers ++=
  Seq(
    "jitpack" at "https://jitpack.io"
//    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  )

val logDeps =
  Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
    "ch.qos.logback" % "logback-classic" % "1.2.3"
  )

val deps = Seq(
  "org.nd4j" % "nd4j-native-platform" % "0.9.1",
  "org.scalatest" %% "scalatest" % "3.0.3",
  "com.doofin" %% "stdscala" % "0.1-SNAPSHOT",
  "org.platanios" %% "tensorflow" % "0.4.1" classifier "linux-cpu-x86_64",
  "org.deeplearning4j" % "deeplearning4j-core" % "0.9.1"
) ++ logDeps
//https://www.scala-sbt.org/1.x/docs/In-Process-Classloaders.html
classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat

lazy val root = (project in file(".")).settings(
  name := "scorch",
  libraryDependencies ++= deps ++ (CrossVersion
    .partialVersion(scalaVersion.value) match {
    case Some((2, major)) if major <= 12 =>
      Seq()
    case _ =>
      Seq(
        "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.0"
      )
  })
)

//    "com.github.doofin" %% "stdscala" % "master-SNAPSHOT"
//    "com.github.doofin" %% "stdScala" % "master-SNAPSHOT"

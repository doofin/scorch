resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

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
    "be.botkop" %% "numsca" % "0.1.5",
    "org.scalatest" %% "scalatest" % "3.0.3"
  )
)

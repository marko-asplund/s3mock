name := "s3mock"

version := "0.7.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.13.2"

crossScalaVersions in ThisBuild := Seq("2.12.10", "2.13.2")

val pekkoVersion = "1.0.1"
val pekkoHttpVersion = "1.0.0"

libraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-stream" % pekkoVersion,
  "org.apache.pekko" %% "pekko-http" % pekkoHttpVersion,
  "org.apache.pekko" %% "pekko-stream-testkit" % pekkoVersion % Test,
  "org.scala-lang.modules" %% "scala-xml" % "1.3.1",
  "org.scala-lang.modules" %% "scala-collection-compat" % "2.11.0",
  "com.github.pathikrit" %% "better-files" % "3.9.2",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.12.620",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test",
  "ch.qos.logback" % "logback-classic" % "1.3.14" % "test",
  "org.iq80.leveldb" % "leveldb" % "0.12",
  "org.apache.pekko" %% "pekko-connectors-s3" % pekkoVersion % "test"
)

libraryDependencies ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, major)) if major >= 13 =>
      Seq("org.scala-lang.modules" %% "scala-parallel-collections" % "0.2.0" % "test")
    case _ =>
      Seq()
  }
}

parallelExecution in Test := false

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

dockerRepository := Some("io.github.marko-asplund")
dockerBaseImage := "eclipse-temurin:11-jre"
dockerExposedPorts := Seq(8001)

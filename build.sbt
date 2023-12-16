name := "s3mock"

version := "0.6.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.13.2"

crossScalaVersions in ThisBuild := Seq("2.12.10", "2.13.2")

val pekkoVersion = "1.0.1"
val pekkoHttpVersion = "1.0.0"

libraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-stream" % pekkoVersion,
  "org.apache.pekko" %% "pekko-http" % pekkoHttpVersion,
  "org.apache.pekko" %% "pekko-stream-testkit" % pekkoVersion % Test,
  "org.scala-lang.modules" %% "scala-xml" % "1.3.0",
  "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.6",
  "com.github.pathikrit" %% "better-files" % "3.9.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.294",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "test",
  "org.iq80.leveldb" % "leveldb" % "0.12",
  "org.apache.pekko" %% "pekko-connectors-s3" % pekkoVersion % "test",
  "javax.xml.bind" % "jaxb-api" % "2.3.0",
  "com.sun.xml.bind" % "jaxb-core" % "2.3.0",
  "com.sun.xml.bind" % "jaxb-impl" % "2.3.0"
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

enablePlugins(DockerPlugin)
assemblyJarName in assembly := "s3mock.jar"
mainClass in assembly := Some("io.findify.s3mock.Main")
test in assembly := {}

dockerfile in docker := new Dockerfile {
  from("adoptopenjdk/openjdk11:jre-11.0.7_10-debian")
  expose(8001)
  add(assembly.value, "/app/s3mock.jar")
  entryPoint(
      "java", 
      "-Xmx128m", 
      "-jar", 
      "--add-opens",
      "java.base/jdk.internal.ref=ALL-UNNAMED",
      "/app/s3mock.jar"
  )
}
imageNames in docker := Seq(
  ImageName(s"findify/s3mock:${version.value.replaceAll("\\+", "_")}"),
  ImageName(s"findify/s3mock:latest")
)

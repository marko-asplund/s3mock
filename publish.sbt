organization := "io.github.marko-asplund"
organizationName := "marko-asplund"
organizationHomepage := Some(url("https://github.com/marko-asplund/"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/marko-asplund/s3mock"),
    "scm:git:git@github.com:marko-asplund/s3mock.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id = "romangrebennikov",
    name = "Roman Grebennikov",
    email = "your@email",
    url = url("http://www.dfdx.me")
  ),
  Developer(
    id = "marko-asplund",
    name = "Marko Asplund",
    email = "your@email",
    url = url("https://github.com/marko-asplund")
  )
)

ThisBuild / description := "Embedded S3 server for easy mocking"
ThisBuild / licenses := List(
  "MIT" -> url("https://opensource.org/licenses/MIT")
)
ThisBuild / homepage := Some(url("https://github.com/marko-asplund/s3mock"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  // For accounts created after Feb 2021:
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true

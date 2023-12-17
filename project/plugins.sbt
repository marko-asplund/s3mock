logLevel := Level.Warn

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.3")
addDependencyTreePlugin
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.10.0")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.2.1")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.1.0")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.1.5")
addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.11.0")

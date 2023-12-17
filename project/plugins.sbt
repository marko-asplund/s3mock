logLevel := Level.Warn

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.3")
addDependencyTreePlugin
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.10.0")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.2.1")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.1.0")

addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")

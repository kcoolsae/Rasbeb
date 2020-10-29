// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.11")
addSbtPlugin("com.typesafe.sbt" % "sbt-play-enhancer" % "1.2.2")

// addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.0")

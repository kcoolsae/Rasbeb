// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.11")
    // bumping up to 2.5 really does not work.... needs higher version of scala and of sbt
    // and then other things start to fail...

addSbtPlugin("com.typesafe.sbt" % "sbt-play-enhancer" % "1.2.2")

// addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.0")

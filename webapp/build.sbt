import sbt.Keys._

name := "Bebras web application - web tier"

version := "1.1-SNAPSHOT"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaCore,
  javaJdbc,
  javaWs
)

// For Hikari

resolvers += Resolver.url("Edulify Repository", url("http://edulify.github.io/modules/releases/"))(Resolver.ivyStylePatterns)

libraryDependencies ++= Seq(
  "be.bebras.rasbeb" % "db" % "1.2-SNAPSHOT",
  "org.springframework" % "spring-beans" % "4.1.1.RELEASE", // needed for recursive direct field binding
  "org.springframework" % "spring-context" % "4.1.1.RELEASE", // to be compatible with the above
  "com.typesafe.play" %% "play-mailer" % "4.0.0",
  "org.apache.poi" % "poi-ooxml" % "3.9"
)

// The following are needed at runtime because the Hibernate Validator needs them. Hopefully they
// can be thrown out with later version of play?
libraryDependencies ++= Seq(
  "javax.activation" % "activation" % "1.1.1" % Runtime,
  "javax.xml.bind" % "jaxb-api" % "2.3.0" % Runtime,
  "com.sun.xml.bind" % "jaxb-core" % "2.3.0" % Runtime,
  "com.sun.xml.bind" % "jaxb-impl" % "2.3.0" % Runtime
)

// Correct Java Level
javacOptions in Compile ++= Seq("-source", "1.8", "-target", "1.8", "-encoding", "UTF-8", "-Xlint", "-Xlint:-processing")

lazy val root = (project in file(".")).enablePlugins(PlayJava)

TwirlKeys.templateImports ++= Seq(
  "bindings._", "snippets._", "be.bebras.rasbeb.db.data._", "be.bebras.rasbeb.db.dao._"
)

play.sbt.routes.RoutesKeys.routesImport ++= Seq(
  "bindings._", "bindings.Binders._", "be.bebras.rasbeb.db.data._"
)

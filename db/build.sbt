name := "Bebras web application - db tier"

normalizedName := "db"

version := "1.2-SNAPSHOT"

organization := "be.bebras.rasbeb"

crossPaths := false

libraryDependencies += "org.postgresql" % "postgresql" % "42.2.18" // not Runtime because we refer to PGSimpleDatasource somewhere
libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"

autoScalaLibrary := false

parallelExecution in Test := false

javacOptions in Compile ++= Seq("-source", "1.8", "-target", "1.8", "-encoding", "UTF-8", "-Xlint", "-Xlint:-processing")
publishArtifact in (Compile, packageDoc) := false // otherwise there is a javadoc error

//unmanagedSourceDirectories in Compile += baseDirectory.value / "src"/ "main" / "sql"
//
//includeFilter in (Compile, unmanagedSources) := "*.scala" || "*.java" || "*.sql"
//

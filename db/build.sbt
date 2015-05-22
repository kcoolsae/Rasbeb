name := "Bebras web application - db tier"

normalizedName := "db"

version := "1.2-SNAPSHOT"

organization := "be.bebras.rasbeb"

crossPaths := false

libraryDependencies += "postgresql" % "postgresql" % "9.1-901.jdbc4"

libraryDependencies += "com.novocode" % "junit-interface" % "0.10" % "test"

autoScalaLibrary := false

parallelExecution in Test := false

//unmanagedSourceDirectories in Compile += baseDirectory.value / "src"/ "main" / "sql"
//
//includeFilter in (Compile, unmanagedSources) := "*.scala" || "*.java" || "*.sql"
//

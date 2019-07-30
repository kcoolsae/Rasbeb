name := "Bebras web application - db tier"

normalizedName := "db"

version := "1.2-SNAPSHOT"

organization := "be.bebras.rasbeb"

crossPaths := false

libraryDependencies += "org.postgresql" % "postgresql" % "42.2.6" % "run"

libraryDependencies += "com.novocode" % "junit-interface" % "0.10" % "test"

autoScalaLibrary := false

parallelExecution in Test := false

//unmanagedSourceDirectories in Compile += baseDirectory.value / "src"/ "main" / "sql"
//
//includeFilter in (Compile, unmanagedSources) := "*.scala" || "*.java" || "*.sql"
//

name := "Bebras web application - mail testing"

normalizedName := "testmail"

version := "1.0-SNAPSHOT"

organization := "be.bebras.rasbeb"

crossPaths := false

libraryDependencies += "org.subethamail" % "subethasmtp" % "3.1.7"

autoScalaLibrary := false

parallelExecution in Test := false
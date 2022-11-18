ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.3"

lazy val root = (project in file("."))
  .settings(
    name := "cats-effect-cps-example"
  )

libraryDependencies += "org.typelevel" %% "cats-effect-cps" % "0.3.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "3.1.1"
libraryDependencies += "org.fusesource.jansi" % "jansi" % "2.4.0"

name := "zzzbot"

version := "0.1.0"

scalaVersion := "2.12.4"

libraryDependencies ++=
  "pircbot" % "pircbot" % "1.5.0" ::
  "com.github.gilbertw1" %% "slack-scala-client" % "0.2.2" ::
  "org.scala-lang" % "scala-compiler" % "2.12.4" ::
  Nil

assembleArtifact in packageBin := false

scalacOptions ++=
  "-feature" ::
  "-language:_" ::
  "-deprecation" ::
  Nil

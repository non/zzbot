name := "zzbot"

version := "0.1.0"

scalaVersion := "2.12.4"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++=
  "pircbot" % "pircbot" % "1.5.0" ::
  "com.github.gilbertw1" %% "slack-scala-client" % "0.2.2" ::
  "org.scala-lang" % "scala-compiler" % "2.12.4" ::
  "org.typelevel" %% "cats-core" % "1.0.1" ::
  "org.typelevel" %% "cats-free" % "1.0.1" ::
  "org.typelevel" %% "alleycats-core" % "1.0.1" ::
  "org.typelevel" %% "cats-effect" % "0.5" ::
  "org.typelevel" %% "mouse" % "0.16" ::
  "com.chuusai" %% "shapeless" % "2.3.3" ::
  "io.circe" %% "circe-core" % "0.9.0" ::
  "io.circe" %% "circe-generic" % "0.9.0" ::
  "io.circe" %% "circe-parser" % "0.9.0" ::
  "org.scalacheck" %% "scalacheck" % "1.13.5" ::
  // TODO: in theory spire's older version of cats-kernel
  // could be a problem. in practice it seems fine.
  "org.typelevel" %% "spire" % "0.14.1" ::
  "org.spire-math" %% "sortilege" % "0.4.0" ::
  "org.spire-math" %% "zillion" % "0.3.0" ::
  // these are nice-to-have tools for doing approximate measurements
  ("org.spire-math" %% "clouseau" % "0.1-SNAPSHOT" from s"file:///${System.getProperty("user.dir")}plugins/clouseau_2.12-0.1-SNAPSHOT.jar") ::
  "com.github.ichoran" %% "thyme" % "0.1.2-SNAPSHOT" ::
  Nil

fork in run := true

mainClass in assembly := Some("zzbot.Main")

scalacOptions ++=
  "-feature" ::
  "-language:_" ::
  "-deprecation" ::
  Nil

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.8"

val AkkaVersion = "2.6.19"
val LogbackVersion = "1.2.9"

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
    "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
    "ch.qos.logback" % "logback-classic" % LogbackVersion,
  )
)

lazy val testDependencies = Seq(
  "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.2.12" % Test
)



lazy val root = (project in file("."))
  .settings(
    name := "AkkaEssentials",
    commonSettings,
    libraryDependencies ++= testDependencies
  )

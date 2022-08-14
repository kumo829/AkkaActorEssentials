ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.8"

val akkaVersion = "2.6.19"

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
  )
)

lazy val testDependencies = Seq(
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.2.12" % Test
)



lazy val root = (project in file("."))
  .settings(
    name := "AkkaEssentials",
    commonSettings,
    libraryDependencies ++= testDependencies
  )

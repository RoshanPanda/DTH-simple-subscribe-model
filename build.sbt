
val info = new {
  val organization = "com.roshan"
  val name         = "dth-subscription"
  val scalaVersion = "2.12.13"
}

lazy val versions  = new {
  val akka = "2.6.14"
  val akkaHttp = "10.1.11"//"10.2.9"
  val alpakka = "3.0.4"
  val logback = "1.2.3"
  val logbackEcsEncoder = "1.0.1"
  val cats = "2.3.0"
  val slick = "3.3.3"
  val h2db = "1.4.200"
  val jaxbVersion = "2.3.3"
  val postgresJdbc = "9.4-1200-jdbc41"
  val scalaLogging = "3.9.2"
  val dispatchVersion = "0.11.3"
  val testcontainersScalaVersion = "0.39.3"
  val scalactic = "3.2.3"
  val scalatest = "3.2.3"
  val mockServer = "5.11.2"

}

name := info.name
ThisBuild / organization := info.organization
ThisBuild / scalaVersion := info.scalaVersion

lazy val root = (project in file("."))
  .settings(
    name := "dth-subscription-model",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http-spray-json" % versions.akkaHttp,
      "org.typelevel" %% "cats-core" % versions.cats,
      "com.typesafe.akka" %% "akka-actor-typed" % versions.akka,
      "com.typesafe.akka" %% "akka-testkit" % versions.akka,
      "com.typesafe.akka" %% "akka-stream-testkit" % versions.akka,
      "com.typesafe.akka" %% "akka-stream" % versions.akka,
      "com.typesafe.scala-logging" %% "scala-logging" % versions.scalaLogging,
      "org.scalatest" %% "scalatest" % versions.scalatest % Test,
      "org.mock-server" % "mockserver-netty" % versions.mockServer % Test,
      "com.typesafe.akka" %% "akka-http2-support" % versions.akkaHttp,
      "com.typesafe.akka" %% "akka-http-jackson" % versions.akkaHttp,
      "com.pauldijou" %% "jwt-spray-json" % "5.0.0"
    )
  )

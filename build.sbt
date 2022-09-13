import Dependencies._

lazy val scalaV = "3.2.0"
version      := "0.0.1"
scalaVersion := scalaV

lazy val service = (project in file("service"))
  .enablePlugins(JavaServerAppPackaging, DockerPlugin)
  .settings(libraryDependencies ++= serviceDependencies)
  .settings(
    scalaVersion        := scalaV,
    name                := "tracerboy",
    Compile / mainClass := Some("com.pinkstack.tracerboy.TracerboyApp"),
    fork / run          := true,
    Test / fork         := true,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    addCommandAlias("run", "service/run")
  )
  .settings(DockerSettings.settings: _*)

lazy val integration = (project in file("integration"))
  .enablePlugins(GatlingPlugin)
  .settings(libraryDependencies ++= integrationDependencies)
  .settings(
    scalaVersion := scalaV
  )

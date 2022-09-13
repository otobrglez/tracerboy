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

/*
lazy val root = (project in file("."))
  .enablePlugins(JavaServerAppPackaging, DockerPlugin, GatlingPlugin)
  .settings(
    name                := "tracerboy",
    Compile / mainClass := Some("com.pinkstack.tracerboy.TracerboyApp"),
    fork / run          := true,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
  .settings(
    libraryDependencies := Seq(
      // Persistence and PG
      "com.zaxxer"     % "HikariCP"       % "5.0.1",
      "org.flywaydb"   % "flyway-core"    % "9.3.0",
      "org.postgresql" % "postgresql"     % "42.5.0",
      "io.getquill"   %% "quill-jdbc-zio" % "4.4.1",

      // Logging
      "dev.zio"       %% "zio-logging"       % "2.1.1",
      "dev.zio"       %% "zio-logging-slf4j" % "2.1.1",
      "ch.qos.logback" % "logback-classic"   % "1.4.0",

      // Bug
      // "io.suzaku"                  %% "boopickle"          % "1.4.0" force (),
      // "org.scala-lang.modules"     %% "scala-java8-compat" % "1.0.2" force (),
      // "com.typesafe.scala-logging" %% "scala-logging"      % "3.9.5" force (),

      // Http
      "io.d11" %% "zhttp" % "2.0.0-RC11" // TODO: This will become "zio-http"
    ) ++ Seq(
      "dev.zio" %% "zio",
      "dev.zio" %% "zio-test",
      "dev.zio" %% "zio-test-sbt",
      "dev.zio" %% "zio-streams",
      "dev.zio" %% "zio-test-junit"
    ).map(_ % zioVersion) ++ (Seq(
      "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % "test,it",
      "io.gatling"            % "gatling-test-framework"    % gatlingVersion % "test,it"
    ).map(
      // _.excludeAll()
      // _.cross(CrossVersion.for3Use2_13)
      // _.exclude("com.softwaremill.quicklens" % "quicklens")
      _.excludeAll(
        //   ExclusionRule(organization = "org.scala-lang.modules"),
        //   ExclusionRule(organization = "io.suzaku"),
        ExclusionRule(organization = "com.typesafe.scala-logging")
      )
    )) ++ Seq(
      "io.suzaku"                  %% "boopickle"          % "1.4.0" force (),
      "org.scala-lang.modules"     %% "scala-java8-compat" % "1.0.2" force (),
      "com.typesafe.scala-logging" %% "scala-logging"      % "3.9.5" force ()
    ),
    resolvers           := Seq(
      "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases",
      "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      "Sonatype staging" at "https://oss.sonatype.org/content/repositories/staging",
      "Java.net Maven2 Repository" at "https://download.java.net/maven/2/"
    )
  )
  .settings(DockerSettings.settings: _*)
 */

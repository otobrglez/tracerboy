import sbt._

object Dependencies {
  type Version = String
  type Modules = Seq[ModuleID]

  object Versions {
    val zio: Version            = "2.0.2"
    val zioLogging: Version     = "2.1.1"
    val gatling: Version        = "3.8.3"
    val testcontainers: Version = "1.17.3"
  }

  lazy val database: Modules = Seq(
    "com.zaxxer"     % "HikariCP"       % "5.0.1",
    "org.flywaydb"   % "flyway-core"    % "9.3.0",
    "org.postgresql" % "postgresql"     % "42.5.0",
    "io.getquill"   %% "quill-jdbc-zio" % "4.4.1"
  )

  lazy val logging: Modules = Seq(
    "ch.qos.logback" % "logback-classic" % "1.4.0"
  ) ++ Seq(
    "dev.zio" %% "zio-logging",
    "dev.zio" %% "zio-logging-slf4j"
  ).map(_ % Versions.zioLogging)

  lazy val zio: Modules = Seq(
    "dev.zio" %% "zio",
    "dev.zio" %% "zio-test",
    "dev.zio" %% "zio-test-sbt",
    "dev.zio" %% "zio-streams",
    "dev.zio" %% "zio-test-junit"
  ).map(_ % Versions.zio) ++ Seq(
    "io.d11" %% "zhttp" % "2.0.0-RC11" // TODO: This will become "zio-http"
  )

  lazy val gatling: Modules = Seq(
    "io.gatling.highcharts" % "gatling-charts-highcharts",
    "io.gatling"            % "gatling-test-framework"
  ).map(_ % Versions.gatling % "test,it")

  lazy val sharedDependencies: Modules = Seq.empty

  lazy val serviceDependencies: Modules     = sharedDependencies ++ zio ++ logging ++ database
  lazy val integrationDependencies: Modules = sharedDependencies ++ gatling

  lazy val projectResolvers = Seq(
    "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases",
    "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    "Sonatype staging" at "https://oss.sonatype.org/content/repositories/staging",
    "Java.net Maven2 Repository" at "https://download.java.net/maven/2/"
  )
}

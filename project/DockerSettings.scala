import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.docker._
import com.typesafe.sbt.packager.docker.Cmd
import sbt.Compile
import sbt.Keys._
import sbt.Def._
import sbt.librarymanagement._

object DockerSettings {
  val settings: Seq[sbt.Def.SettingsDefinition] = Seq(
    Universal / mappings += (Compile / packageBin).value -> "tracerboy.jar",
    dockerUsername                                       := Some("otobrglez"),
    dockerUpdateLatest                                   := true,
    dockerBaseImage                                      := "azul/zulu-openjdk:17-jre",
    dockerRepository                                     := Some("ghcr.io"),
    dockerExposedPorts                                   := Seq(9090),
    dockerExposedUdpPorts                                := Seq.empty[Int],
    packageName                                          := "tracerboy",
    dockerCommands                                       := dockerCommands.value.flatMap {
      case add @ Cmd("RUN", args @ _*) if args.contains("id") =>
        List(
          Cmd("LABEL", "maintainer Oto Brglez <otobrglez@gmail.com>"),
          Cmd("LABEL", "org.opencontainers.image.url https://github.com/otobrglez/loglog"),
          Cmd("LABEL", "org.opencontainers.image.source https://github.com/otobrglez/loglog"),
          Cmd("ENV", "SBT_VERSION", sbtVersion.value),
          Cmd("ENV", "SCALA_VERSION", scalaVersion.value),
          Cmd("ENV", "TRACERBOY_VERSION", version.value),
          add
        )
      case other                                              => List(other)
    }
    /*
    Docker / publish      := {},
    Docker / publishLocal := {}
     */
  )
}

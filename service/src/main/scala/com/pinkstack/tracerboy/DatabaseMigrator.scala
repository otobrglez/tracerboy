package com.pinkstack.tracerboy

import zio.{System, ZIO}
import ZIO.{fromOption, logInfo}
import org.flywaydb.core.Flyway

object DatabaseMigrator:
  def migrate: ZIO[Any, Throwable, Unit] =
    for
      databaseURLOpt <- System.env("DATABASE_URL").orDie
      databaseURL    <- fromOption(databaseURLOpt)
        .orElseFail(new RuntimeException("Missing the \"DATABASE_URL\" environment variable."))
      flyway         <- ZIO.attempt(
        Flyway
          .configure()
          .cleanDisabled(false)
          .dataSource(databaseURL, null, null)
          .load()
      )
      _              <- ZIO.attempt {
        flyway.clean()
        flyway.migrate()
      }
    yield ()

package com.pinkstack.tracerboy

import zio.ZIO
import zio.{ZIO, *}
import ZIO.{attempt, fromOption, logInfo}
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}

object QuillDataSource:
  private def mkDataSource: Task[HikariDataSource] =
    for
      databaseURLOpt <- System.env("DATABASE_URL").orDie
      databaseURL    <- fromOption(databaseURLOpt)
        .orElseFail(new RuntimeException("Missing the \"DATABASE_URL\" environment variable."))
      pgDataSource   <- attempt {
        val dataSource = new org.postgresql.ds.PGSimpleDataSource()
        dataSource.setURL(databaseURL)
        dataSource
      }
      hikariConfig   <- attempt {
        val config = new HikariConfig()
        config.setDataSource(pgDataSource)
        config
      }
      dataSource     <- attempt(new HikariDataSource(hikariConfig))
    yield dataSource

  val live: ZLayer[Any, Throwable, HikariDataSource] =
    ZLayer.fromZIO(mkDataSource)

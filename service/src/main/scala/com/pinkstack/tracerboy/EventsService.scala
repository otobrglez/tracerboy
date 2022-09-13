package com.pinkstack.tracerboy

import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.ZIO.succeed
import zio.{ZIO, *}

import java.sql.SQLException
import java.util.UUID

type HourlyAnalyticsReport = String

trait EventsService:
  def insert(event: Event): ZIO[Any, SQLException, Boolean]
  def hourlyAnalyticsReport: ZIO[Any, SQLException, HourlyAnalyticsReport]

class EventsServiceLive(quill: Quill.Postgres[SnakeCase]) extends EventsService:
  private case class ReportRow(kpi: String)

  import quill.*

  def insert(event: Event): ZIO[Any, SQLException, Boolean] =
    run(quote(querySchema[Event]("events").insertValue(lift(event))).returning(_ => true))

  def hourlyAnalyticsReport: ZIO[Any, SQLException, HourlyAnalyticsReport] =
    for
      metrics <- run(quote(querySchema[ReportRow]("hourly_analytics")))
      report  <- succeed(metrics.foldLeft("") { case (agg, ReportRow(kpi)) => agg ++ s"$kpi\n" })
    yield report

object EventsService:
  def insert(event: Event): ZIO[EventsService, SQLException, Boolean] =
    ZIO.serviceWithZIO[EventsService](_.insert(event))

  def hourlyAnalyticsReport: ZIO[EventsService, SQLException, HourlyAnalyticsReport] =
    ZIO.serviceWithZIO[EventsService](_.hourlyAnalyticsReport)

  val live = ZLayer.fromFunction(new EventsServiceLive(_))

package com.pinkstack.tracerboy

import com.pinkstack.tracerboy
import zhttp.http.*
import zhttp.http.Status.NoContent
import zhttp.service.Server
import zio.{ZIO, ZIOAppDefault}
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill

object TracerboyApp extends ZIOAppDefault:
  def postEvent(request: Request): ZIO[EventsService, Throwable, Response] =
    for
      event  <- EventParser.parse(request)
      events <- ZIO.service[EventsService]
      _      <- events.insert(event)
    yield Response.ok.setStatus(NoContent)

  def getAnalyticsReport: ZIO[EventsService, Throwable, Response] =
    for
      events <- ZIO.service[EventsService]
      report <- events.hourlyAnalyticsReport
    yield Response.text(report)

  val analyticsApp: HttpApp[EventsService, Throwable] =
    Http.collectZIO[Request] {
      case request @ Method.POST -> _ / "analytics" => postEvent(request)
      case Method.GET -> _ / "analytics"            => getAnalyticsReport
    }

  def app(port: Int = 9090) =
    (DatabaseMigrator.migrate <*> Server.start(port, analyticsApp))
      .provide(
        EventsService.live,
        Quill.Postgres.fromNamingStrategy(SnakeCase),
        QuillDataSource.live
      )
      .debug("TracerboyApp")
      .exitCode

  def run = app()

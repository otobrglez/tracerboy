package com.pinkstack.tracerboy

import com.pinkstack.tracerboy
import zhttp.http.*
import zhttp.http.Status.{InternalServerError, NoContent, UnprocessableEntity}
import zhttp.service.Server
import zio.ZIO.{attempt, fail, fromOption, fromTry, succeed}
import zio.{UIO, ZIO, ZIOAppDefault}
import io.getquill.*
import io.getquill.jdbczio.Quill
import RequestExtensions.queryParameterAs

import java.sql.SQLException

object TracerboyApp extends ZIOAppDefault:

  val createEvent: Http[EventsService, Nothing, Event, Unit] =
    Http.collect[Event] { case event =>
      println(s"com.pinkstack.tracerboy.Event -> ${event}")
    }

  val analyticsApp: HttpApp[EventsService, Throwable] =
    Http.collectHttp[Request] {
      case Method.POST -> _ / "analytics" =>
        createEvent
          .contramapZIO(EventParser.parse)
          .foldHttp(
            th =>
              Http(
                Response
                  .text(s"${th.getClass.getName} - ${th.getMessage}")
                  .setStatus(UnprocessableEntity)
              ),
            de => Http(Response.text(de.getMessage).setStatus(InternalServerError)),
            _ => Http(Response.ok.setStatus(NoContent)),
            Http(Response.ok.setStatus(NoContent))
          )
      case Method.GET -> _ / "analytics"  =>
        Http(Response.text("Analytics goes here."))
    }

  def postEvent(request: Request): ZIO[EventsService, Throwable, Response] =
    for
      event  <- EventParser.parse(request)
      events <- ZIO.service[EventsService]
      _      <- events.insert(event)
    yield Response.ok.setStatus(NoContent)

  val analyticsApp2: HttpApp[EventsService, Throwable] =
    Http
      .collectZIO[Request] {
        case request @ Method.POST -> _ / "analytics" => postEvent(request)
        case Method.GET -> _ / "analytics"            =>
          ZIO.succeed(Response.text("ok"))
      }

  def app(port: Int = 9090) =
    (DatabaseMigrator.migrate <*> Server.start(port, analyticsApp2))
      .provide(
        EventsService.live,
        Quill.Postgres.fromNamingStrategy(SnakeCase),
        QuillDataSource.live
      )
      .debug("TracerboyApp")
      .exitCode

  def run = app()

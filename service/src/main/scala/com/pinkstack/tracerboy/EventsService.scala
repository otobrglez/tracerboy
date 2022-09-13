package com.pinkstack.tracerboy

import io.getquill.*
import io.getquill.jdbczio.Quill
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.ZIO
import zio.{ZIO, *}

import java.sql.{Date, SQLException}
import java.time.{Instant, LocalDate, LocalDateTime, ZoneId, ZoneOffset, ZonedDateTime}
import java.util.UUID

class EventsService(quill: Quill.Postgres[SnakeCase]):
  import quill.*

  def getEvents: ZIO[Any, SQLException, List[Event]] = run(
    query[Event]
  )

  def insert(event: Event): ZIO[Any, SQLException, Boolean] =
    run(
      quote(
        querySchema[Event]("events")
          .insertValue(lift(event))
      ).returning(_ => true)
    )

object EventsService:
  def getEvents: ZIO[EventsService, SQLException, List[Event]] =
    ZIO.serviceWithZIO[EventsService](_.getEvents)

  def insert(event: Event): ZIO[EventsService, SQLException, Boolean] =
    ZIO.serviceWithZIO[EventsService](_.insert(event))

  val live = ZLayer.fromFunction(new EventsService(_))

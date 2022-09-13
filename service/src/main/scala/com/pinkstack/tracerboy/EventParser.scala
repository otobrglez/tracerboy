package com.pinkstack.tracerboy

import com.pinkstack.tracerboy.RequestExtensions.*
import zhttp.http.Request
import zio.ZIO
import zio.ZIO.{fromOption, succeed}

import java.time.LocalDateTime
import scala.Option.{unless, when}

object EventParser:
  final case class MissingUsername(message: String)  extends Throwable(message)
  final case class InvalidEventKind(message: String) extends Throwable(message)
  type ValidationError = MissingUsername | InvalidEventKind | Throwable

  val parse: Request => ZIO[Any, ValidationError, Event] = request =>
    for
      timestamp   <- request.queryParameterAs[LocalDateTime]("timestamp")
      usernameRaw <- request.queryParameterAs[String]("user")
      username    <- fromOption(unless(usernameRaw.isEmpty)(usernameRaw))
        .orElseFail(MissingUsername(s"Username is missing."))
      kindString  <- request.queryParameterAs[String]("event")
      kind        <- fromOption(when(Event.validKinds.contains(kindString))(kindString))
        .orElseFail(InvalidEventKind(s"Invalid event type \"$kindString\""))
    yield Event(kind, timestamp, username)

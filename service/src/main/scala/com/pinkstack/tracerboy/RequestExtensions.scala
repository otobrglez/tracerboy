package com.pinkstack.tracerboy

import zhttp.http.Request
import zio.ZIO
import zio.ZIO.{attempt, fromOption, fromTry, succeed}

import java.time.{Instant, LocalDateTime, ZoneId}
import java.util.UUID
import scala.util.Try

private object ParameterDecoders:
  trait ParameterDecoder[T]:
    def fromString(raw: String): ZIO[Any, Throwable, T]

  given ParameterDecoder[String] with
    def fromString(raw: String): ZIO[Any, Throwable, String] = succeed(raw)

  given ParameterDecoder[UUID] with
    def fromString(raw: String): ZIO[Any, Throwable, UUID] = fromTry(Try(UUID.fromString(raw)))

  given ParameterDecoder[Long] with
    def fromString(raw: String): ZIO[Any, Throwable, Long] = attempt(raw.toLong)

  given ParameterDecoder[LocalDateTime] with
    def fromString(raw: String): ZIO[Any, Throwable, LocalDateTime] =
      for
        timeInMilli   <- attempt(raw.toLong)
        localDateTime <- attempt(
          LocalDateTime.ofInstant(Instant.ofEpochMilli(timeInMilli), ZoneId.systemDefault())
        )
      yield localDateTime

object RequestExtensions:
  import ParameterDecoders.*

  final case class MissingQueryParameter(message: String) extends Throwable(message)

  extension (request: Request)
    def queryParameter(key: String): ZIO[Any, MissingQueryParameter, String] =
      fromOption(request.url.queryParams.get(key).flatMap(_.headOption))
        .orElseFail(MissingQueryParameter(s"Missing query parameter \"$key\"."))

    def queryParameterAs[T](key: String)(using decoder: ParameterDecoder[T]): ZIO[Any, Throwable, T] =
      queryParameter(key).flatMap(decoder.fromString)

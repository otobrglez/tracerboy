package com.pinkstack.tracerboy

import com.pinkstack.tracerboy.EventParser.{parse, InvalidEventKind, MissingUsername}
import com.pinkstack.tracerboy.RequestExtensions.MissingQueryParameter
import zhttp.http.{Method, Request, URL}
import zio.test.Assertion.*
import zio.test.{assertZIO, Spec}

import java.time.{Instant, LocalDateTime, ZoneId}

object EventParserSpec extends zio.test.junit.JUnitRunnableSpec:
  private def mkRequest(queryParams: (String, String)*): Request =
    Request(method = Method.POST, url = URL.root.setQueryParams(queryParams.map((k, v) => (k, List(v))).toMap))

  def spec: Spec[Any, Nothing] = suite("EventParser")(
    test("should fail on empty payload")(
      assertZIO(parse(mkRequest()).exit)(fails(isSubtype[MissingQueryParameter](anything)))
    ),
    test("should fail on wrong input types")(
      assertZIO(
        parse(mkRequest("timestamp" -> "2x2")).exit
      )(fails(isSubtype[NumberFormatException](hasMessage(containsString("2x2")))))
    ),
    test("should fail on missing user")(
      assertZIO(
        parse(mkRequest("timestamp" -> System.currentTimeMillis().toString)).exit
      )(fails(isSubtype[MissingQueryParameter](hasMessage(containsString("user")))))
    ),
    test("should fail on empty user")(
      assertZIO(
        parse(mkRequest("timestamp" -> System.currentTimeMillis().toString, "user" -> "")).exit
      )(fails(isSubtype[MissingUsername](hasMessage(containsString("is missing.")))))
    ),
    test("should fail on unsupported event kind")(
      assertZIO(
        parse(
          mkRequest(
            "timestamp" -> System.currentTimeMillis().toString,
            "user"      -> "Oto",
            "event"     -> "dodo"
          )
        ).exit
      )(fails(isSubtype[InvalidEventKind](hasMessage(containsString("Invalid event type")))))
    ),
    test("should not fail on valid event") {
      val timestampEpoch: Long = 1_663_062_151_737L
      val localDateTime        = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampEpoch), ZoneId.systemDefault())
      assertZIO(
        parse(
          mkRequest(
            "timestamp" -> timestampEpoch.toString,
            "user"      -> "Oto",
            "event"     -> "click"
          )
        ).exit
      )(succeeds(hasField("timestamp", _.timestamp, equalTo(localDateTime))))
    }
  )

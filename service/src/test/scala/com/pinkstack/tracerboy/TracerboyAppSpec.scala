package com.pinkstack.tracerboy

import com.pinkstack.tracerboy.TracerboyApp.analyticsApp
import io.getquill.jdbczio.Quill
import zhttp.http.*
import zhttp.service.Server
import zio.ZIO.{fail, succeed}
import zio.test.Assertion.*
import zio.test.TestAspect.*
import zio.test.TestSystem.{clearEnv, putEnv}
import zio.test.{assertZIO, Spec}
import zio.{ZIO, ZLayer}

import java.sql.SQLException

object TracerboyAppSpec extends zio.test.junit.JUnitRunnableSpec:
  private def mkRequest(queryParams: (String, String)*): Request =
    Request(method = Method.POST, url = URL.root.setQueryParams(queryParams.map((k, v) => (k, List(v))).toMap))

  def spec = suite("Server app")(
    suite("getAnalyticsReport")(
      test("can crash with SQL exception") {
        assertZIO(TracerboyApp.getAnalyticsReport.exit)(
          fails(isSubtype[SQLException](hasMessage(containsString("SQL crash"))))
        )
      }.provide(
        ZLayer.succeed(new EventsService {
          def insert(event: Event): ZIO[Any, SQLException, Boolean]                = ???
          def hourlyAnalyticsReport: ZIO[Any, SQLException, HourlyAnalyticsReport] = fail(new SQLException("SQL crash"))
        })
      ),
      test("has the right status code - 200") {
        assertZIO(TracerboyApp.getAnalyticsReport.exit)(
          succeeds(isSubtype[Response](hasField("status", _.status, equalTo(Status.Ok))))
        )
      }.provide(
        ZLayer.succeed(new EventsService {
          def insert(event: Event): ZIO[Any, SQLException, Boolean]                = ???
          def hourlyAnalyticsReport: ZIO[Any, SQLException, HourlyAnalyticsReport] = succeed("report")
        })
      )
    ),
    suite("postEvent") {
      test("has status code - 204") {
        val timestampEpoch: Long = 1_663_062_151_737L
        assertZIO(
          TracerboyApp
            .postEvent(
              mkRequest(
                "timestamp" -> timestampEpoch.toString,
                "user"      -> "Oto",
                "event"     -> "click"
              )
            )
            .exit
        )(succeeds(isSubtype[Response](hasField("status", _.status, equalTo(Status.NoContent)))))
      }.provide(
        ZLayer.succeed(new EventsService {
          def insert(event: Event): ZIO[Any, SQLException, Boolean]                = succeed(true)
          def hourlyAnalyticsReport: ZIO[Any, SQLException, HourlyAnalyticsReport] = ???
        })
      )
    }
  )

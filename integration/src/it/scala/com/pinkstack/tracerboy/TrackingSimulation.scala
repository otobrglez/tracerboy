package com.pinkstack.tracerboy

import io.gatling.core.Predef.*
import io.gatling.core.feeder.Feeder
import io.gatling.http.Predef.*

import java.util.UUID
import scala.concurrent.duration.*

class TrackingSimulation extends Simulation:
  // TODO: Use Testcontainers and existing Docker Compose in the future - https://www.testcontainers.org/

  val httpProtocol               = http.baseUrl("http://127.0.0.1:9090")
  val uuidFeeder: Feeder[String] = Iterator.continually(Map("uuid" -> UUID.randomUUID().toString))

  def userBrowsesPage =
    feed(uuidFeeder)
      .exec(
        repeat(80)(
          exec(
            http("post_valid_click_event")
              .post("/analytics")
              .queryParamMap(
                Map("timestamp" -> System.currentTimeMillis().toString, "event" -> "click", "user" -> "#{uuid}")
              )
              .check(status.in(204))
          )
        ).repeat(10)(
          exec(
            http("post_valid_impression_event")
              .post("/analytics")
              .queryParamMap(
                Map("timestamp" -> System.currentTimeMillis().toString, "event" -> "impression", "user" -> "#{uuid}")
              )
              .check(status.in(204))
          ).repeat(10)(
            exec(
              http("get_analytics")
                .get("/analytics")
                .check(status.in(200))
            )
          )
        )
      )

  val regularTraffic = scenario("Regular traffic")
    .exec(userBrowsesPage)
    .pause(1)

  setUp(
    regularTraffic.inject(
      // atOnceUsers(2)
      // rampUsers(1_00).during(60.seconds)
      // constantUsersPerSec(100).during(1.minutes)

      incrementUsersPerSec(6.0)
        .times(3)
        .eachLevelLasting(10)
        .separatedByRampsLasting(10)
        .startingFrom(10) // Double
    )
  ).protocols(httpProtocol)

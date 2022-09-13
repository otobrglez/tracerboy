package com.pinkstack.tracerboy

import java.time.LocalDateTime
import java.util.UUID

final case class Event(
  kind: Event.Kind,
  timestamp: LocalDateTime,
  username: Event.Username,
  event_id: UUID = UUID.randomUUID()
)

object Event:
  type Username = String
  type Kind     = String
  val validKinds: Set[Kind] = Set("click", "impression")

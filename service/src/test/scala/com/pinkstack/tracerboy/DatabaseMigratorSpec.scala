package com.pinkstack.tracerboy

import org.flywaydb.core.api.FlywayException
import zio.test.{assertZIO, Spec}
import zio.test.TestSystem.{clearEnv, putEnv}

import zio.test.Assertion.{containsString, fails, hasMessage, isSubtype}

object DatabaseMigratorSpec extends zio.test.junit.JUnitRunnableSpec:
  def spec: Spec[Any, Nothing] = suite("DatabaseMigrator")(
    test("fail if DATABASE_URL is not set")(
      clearEnv("DATABASE_URL")
        *>
          assertZIO(DatabaseMigrator.migrate.exit)(
            fails(
              isSubtype[RuntimeException](
                hasMessage(containsString("Missing the \"DATABASE_URL\" environment variable"))
              )
            )
          )
    ),
    test("fail if DATABASE_URL is miss-configured")(
      putEnv("DATABASE_URL", "xxx")
        *>
          assertZIO(DatabaseMigrator.migrate.exit)(
            fails(isSubtype[FlywayException](hasMessage(containsString("No database found"))))
          )
    )
  )

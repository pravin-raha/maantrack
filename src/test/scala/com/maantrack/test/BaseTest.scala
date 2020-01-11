package com.maantrack.test

import cats.effect.IO
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

trait BaseTest extends AnyFlatSpec with Matchers {
  implicit val unsafeLogger: SelfAwareStructuredLogger[IO] =
    Slf4jLogger.getLogger[IO]
}

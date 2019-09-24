package com.maantrack.endpoint

import cats.effect.{ ContextShift, IO }
import com.maantrack.Module
import com.maantrack.test.{ BaseTest, TestEmbeddedPostgres }
import org.scalatest.concurrent.Eventually

import scala.concurrent.ExecutionContext

class UserTest extends BaseTest with TestEmbeddedPostgres with Eventually {
  beforeAll()
  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  val module                                  = new Module(currentDb.xa, hasher)

  "/user/register" should "register" in {
    // given
  }
}

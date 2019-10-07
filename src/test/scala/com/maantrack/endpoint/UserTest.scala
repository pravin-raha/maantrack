package com.maantrack.endpoint

import java.time.Instant

import cats.effect.{ ContextShift, IO }
import com.maantrack.Module
import com.maantrack.domain.user.{ Role, UserRequest, UserResponse }
import com.maantrack.test.{ BaseTest, Requests, TestEmbeddedPostgres }
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.{ HttpApp, Uri }
import org.scalatest.concurrent.Eventually
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import tsec.passwordhashers.jca.BCrypt

import scala.concurrent.ExecutionContext

class UserTest
    extends BaseTest
    with TestEmbeddedPostgres
    with Eventually
    with Http4sClientDsl[IO]
    with Http4sDsl[IO]
    with ScalaCheckPropertyChecks {

  implicit var contextShift: ContextShift[IO] = _
  var module: Module[IO, BCrypt]              = _

  private lazy val userRoutes: HttpApp[IO] = {
    Router(("/user", module.userEndpoint)).orNotFound
  }

  private lazy val request = new Requests(module)

  private val userSignup: UserRequest =
    UserRequest("test@test.com", "fname", "lname", Role.Customer, "test", "test", Instant.now)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    contextShift = IO.contextShift(ExecutionContext.global)
    module = new Module(currentDb.xa, hasher)
  }

  "/user/login" should "create user and log in" in {
    val userSignup: UserRequest =
      UserRequest("test@test.com", "fname", "lname", Role.Customer, "test", "test", Instant.now)
    val (_, authorization) = request.signUpAndLogIn(userSignup).unsafeRunSync()
    authorization should be(Symbol("defined"))
  }

  "/user" should "get user by userId" in {
    (for {
      loginResp                    <- request.signUpAndLogIn(userSignup)
      (createdUser, authorization) = loginResp
      getRequest                   <- GET(Uri.unsafeFromString(s"/user/${createdUser.userId}"))
      getRequestAuth               = getRequest.putHeaders(authorization.get)
      getResponse                  <- userRoutes.run(getRequestAuth)
      getUser                      <- getResponse.as[UserResponse]
    } yield {
      getResponse.status shouldEqual Ok
      createdUser.userName shouldEqual getUser.userName
    }).unsafeRunSync
  }

  "/user" should "delete user by userId" in {
    (for {
      loginResp                    <- request.signUpAndLogIn(userSignup)
      (createdUser, authorization) = loginResp
      getRequest                   <- DELETE(Uri.unsafeFromString(s"/user/${createdUser.userId}"))
      getRequestAuth               = getRequest.putHeaders(authorization.get)
      getResponse                  <- userRoutes.run(getRequestAuth)
      getUser                      <- getResponse.as[UserResponse]
    } yield {
      getResponse.status shouldEqual Ok
      createdUser.userName shouldEqual getUser.userName
    }).unsafeRunSync
  }

}

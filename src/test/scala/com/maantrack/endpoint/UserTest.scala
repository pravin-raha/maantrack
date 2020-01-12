package com.maantrack.endpoint

import java.time.Instant

import cats.effect.{ Blocker, ContextShift, IO }
import com.maantrack.Module
import com.maantrack.domain.{ Role, UserRequest, UserResponse }
import com.maantrack.test.{ BaseTest, Requests, TestEmbeddedPostgres }
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.{ AuthScheme, Credentials, HttpApp, Uri }
import org.scalatest.concurrent.Eventually
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.concurrent.ExecutionContext

class UserTest
    extends BaseTest
    with TestEmbeddedPostgres
    with Eventually
    with Http4sClientDsl[IO]
    with Http4sDsl[IO]
    with ScalaCheckPropertyChecks {

  implicit var contextShift: ContextShift[IO] = _
  var module: Module[IO]                      = _

  private lazy val userRoutes: HttpApp[IO] = module.httpApp
  private lazy val request                 = new Requests(module)

  private val userSignup: UserRequest =
    UserRequest("test@test.com", "fname", "lname", Role.Customer, "test", "test", Instant.now)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    contextShift = IO.contextShift(ExecutionContext.global)
    module = new Module(currentDb.xa, Blocker.liftExecutionContext(ExecutionContext.global), jwtConfig)
  }

  "/user/login" should "create user and log in" in {
    val userSignup: UserRequest =
      UserRequest("test@test.com", "fname", "lname", Role.Customer, "test", "test", Instant.now)
    val (_, token) = request.signUpAndLogIn(userSignup).unsafeRunSync()
    token.value should not be empty
  }

  "/user" should "get user by userId" in {
    (for {
      loginResp            <- request.signUpAndLogIn(userSignup)
      (createdUser, token) = loginResp
      getRequest           <- GET(Uri.unsafeFromString(s"/user/${createdUser.userId}"))
      getRequestAuth       = getRequest.putHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, token.value)))
      getResponse          <- userRoutes.run(getRequestAuth)
      getUser              <- getResponse.as[UserResponse]
    } yield {
      getResponse.status shouldEqual Ok
      createdUser.userName shouldEqual getUser.userName
    }).unsafeRunSync
  }

  "/user" should "delete user by userId" in {
    (for {
      loginResp            <- request.signUpAndLogIn(userSignup)
      (createdUser, token) = loginResp
      getRequest           <- DELETE(Uri.unsafeFromString(s"/user/${createdUser.userId}"))
      getRequestAuth       = getRequest.putHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, token.value)))
      getResponse          <- userRoutes.run(getRequestAuth)
      getUser              <- getResponse.as[UserResponse]
    } yield {
      getResponse.status shouldEqual Ok
      createdUser.userName shouldEqual getUser.userName
    }).unsafeRunSync
  }

}

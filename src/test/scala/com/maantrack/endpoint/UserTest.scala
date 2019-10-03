package com.maantrack.endpoint

import java.time.Instant

import cats.effect.{ ContextShift, IO }
import com.maantrack.Module
import com.maantrack.domain.user.{ Role, UserCredential, UserRequest, UserResponse }
import com.maantrack.test.{ BaseTest, TestEmbeddedPostgres }
import io.circe.generic.auto._
import org.http4s.circe.{ jsonEncoderOf, jsonOf }
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.{ EntityDecoder, EntityEncoder, HttpApp, Uri }
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

  implicit val userEnc: EntityEncoder[IO, UserResponse] = jsonEncoderOf
  implicit val userDec: EntityDecoder[IO, UserResponse] = jsonOf

  implicit val signUpRequestEnc: EntityEncoder[IO, UserRequest] = jsonEncoderOf
  implicit val signUpRequestDec: EntityDecoder[IO, UserRequest] = jsonOf

  implicit val userCredentialEnc: EntityEncoder[IO, UserCredential] = jsonEncoderOf
  implicit val userCredentialDec: EntityDecoder[IO, UserCredential] = jsonOf

  lazy val userRoutes: HttpApp[IO] = {
    Router(("/user", module.userEndpoint)).orNotFound
  }

  def signUpAndLogIn(
    userSignUp: UserRequest,
    userEndpoint: HttpApp[IO]
  ): IO[(UserResponse, Option[Authorization])] =
    for {
      signUpRq   <- POST(userSignUp, uri"/user")
      signUpResp <- userEndpoint.run(signUpRq)
      user       <- signUpResp.as[UserResponse]
      loginBody  = UserCredential(userSignUp.userName, userSignUp.password)
      loginRq    <- POST(loginBody, uri"/user/login")
      loginResp  <- userEndpoint.run(loginRq)
    } yield {
      user -> loginResp.headers.get(Authorization)
    }

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    contextShift = IO.contextShift(ExecutionContext.global)
    module = new Module(currentDb.xa, hasher)
  }

  "user/login" should "create user and log in" in {
    val userSignup: UserRequest =
      UserRequest("test@test.com", "fname", "lname", Role.Customer, "test", "test", Instant.now)
    val (_, authorization) = signUpAndLogIn(userSignup, userRoutes).unsafeRunSync()
    authorization should be(Symbol("defined"))
  }

  "user" should "get user by userName" in {
    val userSignup: UserRequest =
      UserRequest("test@test.com", "fname", "lname", Role.Customer, "test", "test", Instant.now)

    (for {
      loginResp                    <- signUpAndLogIn(userSignup, userRoutes)
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

}

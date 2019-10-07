package com.maantrack.test

import cats.effect.IO
import com.maantrack.Module
import com.maantrack.domain.user.{ UserCredential, UserRequest, UserResponse }
import org.http4s.HttpApp
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.implicits._
import org.http4s.server.Router
import tsec.passwordhashers.jca.BCrypt

class Requests(private val module: Module[IO, BCrypt]) extends Http4sDsl[IO] with Http4sClientDsl[IO] {

  private lazy val userRoutes: HttpApp[IO] = {
    Router(("/user", module.userEndpoint)).orNotFound
  }

  def signUpAndLogIn(
    userSignUp: UserRequest
  ): IO[(UserResponse, Option[Authorization])] =
    for {
      signUpRq   <- POST(userSignUp, uri"/user")
      signUpResp <- userRoutes.run(signUpRq)
      user       <- signUpResp.as[UserResponse]
      loginBody  = UserCredential(userSignUp.userName, userSignUp.password)
      loginRq    <- POST(loginBody, uri"/user/login")
      loginResp  <- userRoutes.run(loginRq)
    } yield {
      user -> loginResp.headers.get(Authorization)
    }
}

package com.maantrack.test

import cats.effect.{ IO, Sync }
import com.maantrack.Module
import com.maantrack.domain.board.BoardRequest
import com.maantrack.domain.user.{ UserCredential, UserRequest, UserResponse }
import org.http4s.circe.jsonOf
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.server.Router
import org.http4s.syntax.all._
import org.http4s.{ EntityDecoder, HttpApp, Uri }
import tsec.passwordhashers.jca.BCrypt

class Requests(private val module: Module[IO, BCrypt]) extends Http4sDsl[IO] with Http4sClientDsl[IO] {

  private lazy val userRoutes: HttpApp[IO] = {
    Router(("/user", module.userEndpoint)).orNotFound
  }

  private lazy val boardRoutes: HttpApp[IO] = {
    Router(("/board", module.boardServiceEndpoint)).orNotFound
  }

  implicit def longDecoder[F[_]: Sync]: EntityDecoder[F, Long] = jsonOf

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

  def createAndGetBoard(
    userSignUp: UserRequest,
    boardRequest: BoardRequest
  ): IO[(Long, Option[Authorization])] =
    for {
      loginResp          <- signUpAndLogIn(userSignUp)
      (_, authorization) = loginResp
      postRequest        <- POST(boardRequest, Uri.unsafeFromString(s"/board"))
      postRequestAuth    = postRequest.putHeaders(authorization.get)
      postResponse       <- boardRoutes.run(postRequestAuth)
      getBoardId         <- postResponse.as[Long]
    } yield (getBoardId, authorization)

}

package com.maantrack.test

import cats.effect.{ IO, Sync }
import com.maantrack.Module
import com.maantrack.domain.board.BoardRequest
import com.maantrack.domain.card.CardRequest
import com.maantrack.domain.cardlist.CardListRequest
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
  private lazy val routes: HttpApp[IO] = Router(
    "/user"  -> module.userEndpoint,
    "/board" -> module.boardServiceEndpoint,
    "/list"  -> module.listEndpoint,
    "/card"  -> module.cardEndpoint
  ).orNotFound

  implicit def longDecoder[F[_]: Sync]: EntityDecoder[F, Long] = jsonOf

  def signUpAndLogIn(
    userSignUp: UserRequest
  ): IO[(UserResponse, Option[Authorization])] =
    for {
      signUpRq   <- POST(userSignUp, uri"/user")
      signUpResp <- routes.run(signUpRq)
      user       <- signUpResp.as[UserResponse]
      loginBody  = UserCredential(userSignUp.userName, userSignUp.password)
      loginRq    <- POST(loginBody, uri"/user/login")
      loginResp  <- routes.run(loginRq)
    } yield {
      user -> loginResp.headers.get(Authorization)
    }

  def createAndGetBoard(
    authorization: Option[Authorization],
    boardRequest: BoardRequest
  ): IO[Long] =
    for {
      postRequest     <- POST(boardRequest, Uri.unsafeFromString(s"/board"))
      postRequestAuth = postRequest.putHeaders(authorization.get)
      postResponse    <- routes.run(postRequestAuth)
      getBoardId      <- postResponse.as[Long]
    } yield getBoardId

  def createAndGetCardList(
    authorization: Option[Authorization],
    cardListRequest: CardListRequest
  ): IO[Long] =
    for {
      postRequest     <- POST(cardListRequest, Uri.unsafeFromString(s"/list"))
      postRequestAuth = postRequest.putHeaders(authorization.get)
      postResponse    <- routes.run(postRequestAuth)
      getListId       <- postResponse.as[Long]
    } yield getListId

  def createAndGetCard(
    authorization: Option[Authorization],
    cardRequest: CardRequest
  ): IO[Long] =
    for {
      postRequest     <- POST(cardRequest, Uri.unsafeFromString(s"/card"))
      postRequestAuth = postRequest.putHeaders(authorization.get)
      postResponse    <- routes.run(postRequestAuth)
      getCardId       <- postResponse.as[Long]
    } yield getCardId
}

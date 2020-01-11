package com.maantrack.test

import cats.effect.IO
import com.maantrack.Module
import com.maantrack.domain._
import dev.profunktor.auth.jwt.JwtToken
import io.circe.generic.auto._
import io.circe.{ Decoder, Encoder }
import org.http4s._
import org.http4s.circe.{ jsonEncoderOf, jsonOf }
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.syntax.all._

class Requests(private val module: Module[IO]) extends Http4sDsl[IO] with Http4sClientDsl[IO] {

  private lazy val routes: HttpApp[IO] = module.httpApp

  implicit val decodeJwtToken: Decoder[JwtToken] = Decoder.forProduct1("access_token")(JwtToken.apply)

  implicit def jsonDecoder[A: Decoder]: EntityDecoder[IO, A] = jsonOf[IO, A]
  implicit def jsonEncoder[A: Encoder]: EntityEncoder[IO, A] = jsonEncoderOf[IO, A]

  def signUpAndLogIn(
    userSignUp: UserRequest
  ): IO[(UserResponse, JwtToken)] =
    for {
      signUpRq   <- POST(userSignUp, uri"/user")
      signUpResp <- routes.run(signUpRq)
      user       <- signUpResp.as[UserResponse]
      loginBody  = UserCredential(userSignUp.userName, userSignUp.password)
      loginRq    <- POST(loginBody, uri"/auth/login")
      loginResp  <- routes.run(loginRq)
      token      <- loginResp.as[JwtToken]
    } yield {
      println("=====================================================")
      println(s"Token :: $token")
      println("=====================================================")

      (user, token)
    }

  def createAndGetBoard(
    token: JwtToken,
    boardRequest: BoardRequest
  ): IO[Long] =
    for {
      postRequest     <- POST(boardRequest, Uri.unsafeFromString(s"/board"))
      postRequestAuth = postRequest.putHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, token.value)))
      postResponse    <- routes.run(postRequestAuth)
      getBoardId      <- postResponse.as[Long]
    } yield getBoardId

  def createAndGetCardList(
    token: JwtToken,
    cardListRequest: CardListRequest
  ): IO[Long] =
    for {
      postRequest     <- POST(cardListRequest, Uri.unsafeFromString(s"/list"))
      postRequestAuth = postRequest.putHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, token.value)))
      postResponse    <- routes.run(postRequestAuth)
      getListId       <- postResponse.as[Long]
    } yield getListId

  def createAndGetCard(
    token: JwtToken,
    cardRequest: CardRequest
  ): IO[Long] =
    for {
      postRequest     <- POST(cardRequest, Uri.unsafeFromString(s"/card"))
      postRequestAuth = postRequest.putHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, token.value)))
      postResponse    <- routes.run(postRequestAuth)
      getCardId       <- postResponse.as[Long]
    } yield getCardId

}

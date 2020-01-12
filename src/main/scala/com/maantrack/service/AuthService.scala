package com.maantrack.service

import cats.effect.Sync
import cats.implicits._
import com.maantrack.config.JwtConfig
import com.maantrack.domain.{ InvalidUserOrPassword, User }
import dev.profunktor.auth.jwt.JwtToken
import io.chrisdavenport.log4cats.Logger
import io.circe.generic.auto._
import io.circe.syntax._
import pdi.jwt.{ Jwt, JwtAlgorithm }

class AuthService[F[_]: Sync: Logger](userService: UserService[F], jwtConfig: JwtConfig) {

  def login(username: String, password: String): F[JwtToken] =
    for {
      user <- userService
               .getUserByUserName(username)
               .value
      checkedUser <- user.find(user => Crypto.checkPassword(password, user.password)).pure[F]
      token       <- checkedUser.fold(InvalidUserOrPassword(username).raiseError[F, JwtToken])(u => encode(u).pure[F])
    } yield token

  private def encode(user: User): JwtToken =
    JwtToken(
      Jwt.encode(
        user.asJson.toString(),
        jwtConfig.hmacSecret,
        JwtAlgorithm.HS256
      )
    )

  def logout(token: JwtToken, username: String): F[Unit] = ???
}

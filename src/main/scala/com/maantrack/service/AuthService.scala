package com.maantrack.service

import cats.effect.Sync
import cats.implicits._
import com.maantrack.domain.InvalidUserOrPassword
import dev.profunktor.auth.jwt.JwtToken
import io.chrisdavenport.log4cats.Logger
import io.circe.generic.auto._
import io.circe.syntax._
import pdi.jwt.{ Jwt, JwtAlgorithm }

class AuthService[F[_]: Sync: Logger](userService: UserService[F]) {
  def newUser(username: String, password: String): F[JwtToken] = ???

  def login(username: String, password: String): F[JwtToken] =
    userService
      .findUserByUsernameAndPassword(username, password)
      .value
      .flatMap {
        case Some(user) =>
          JwtToken(
            Jwt.encode(
              user.asJson.toString(),
              "53cr3t",
              JwtAlgorithm.HS256
            )
          ).pure[F]
        case None => InvalidUserOrPassword(username).raiseError[F, JwtToken]
      }

  def logout(token: JwtToken, username: String): F[Unit] = ???
}

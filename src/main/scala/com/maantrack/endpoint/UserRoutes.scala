package com.maantrack.endpoint

import cats.effect._
import cats.syntax.all._
import com.maantrack.domain.{ User, UserRequest, UserResponse, UsernameAlreadyExist }
import com.maantrack.service.{ Crypto, UserService }
import io.chrisdavenport.log4cats.Logger
import io.circe.generic.auto._
import io.circe.syntax._
import io.scalaland.chimney.dsl._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{ AuthMiddleware, Router }
import org.http4s.{ AuthedRoutes, HttpRoutes }

class UserRoutes[F[_]: Sync: Logger](
  userService: UserService[F]
) extends Http4sDsl[F] {

  private val prefixPath = "/user"

  private val userCreateRoutes = HttpRoutes.of[F] {
    case req @ POST -> Root =>
      (for {
        userRequest <- req.as[UserRequest]
        hash        = Crypto.encrypt(userRequest.password)
        userRes <- userService
                    .addUser(userRequest.copy(password = hash))
                    .map(_.into[UserResponse].transform)
        result <- Ok(userRes.asJson)
      } yield result).recoverWith {
        case UsernameAlreadyExist(username) => Conflict(s"Username already exist: $username")
      }
  }

  private val httpRoutes: AuthedRoutes[User, F] = AuthedRoutes.of {
    case GET -> Root / LongVar(userId) as _ =>
      userService
        .getUserById(userId)
        .value
        .flatMap {
          case Some(user) => Ok(user.into[UserResponse].transform.asJson)
          case None       => NotFound(s"User with user id $userId not found".asJson)
        }
    case DELETE -> Root / LongVar(userId) as _ =>
      userService
        .deleteUserById(userId)
        .value
        .flatMap {
          case Some(user) => Ok(user.into[UserResponse].transform.asJson)
          case None       => NotFound(s"User with user id $userId not found".asJson)
        }
  }

  def routes(authMiddleware: AuthMiddleware[F, User]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes),
    prefixPath -> userCreateRoutes
  )

}

object UserRoutes {
  def apply[F[_]: Sync: Logger](
    userService: UserService[F]
  )(
    implicit F: ConcurrentEffect[F]
  ): UserRoutes[F] =
    new UserRoutes(
      userService
    )
}

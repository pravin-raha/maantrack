package com.maantrack.endpoint

import cats.effect._
import cats.implicits._
import com.maantrack.domain.{ User, UserRequest, UserResponse }
import com.maantrack.service.UserService
import io.chrisdavenport.log4cats.Logger
import io.circe.generic.auto._
import io.circe.syntax._
import io.scalaland.chimney.dsl._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{ AuthedRoutes, HttpRoutes }

class UserServiceEndpoint[F[_]: Sync: Logger](
  userService: UserService[F]
) extends Http4sDsl[F] {
  private val userCreateService = HttpRoutes.of[F] {
    case req @ POST -> Root =>
      for {
        userRequest <- req.as[UserRequest]
        hash        = userRequest.password //TODO add new hasher
        userRes <- userService
                    .addUser(userRequest.copy(password = hash))
                    .map(_.into[UserResponse].transform)
        result <- Ok(userRes.asJson)
      } yield result
  }

  private val uService: AuthedRoutes[User, F] = AuthedRoutes.of {
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

  val publicService: HttpRoutes[F]          = userCreateService
  val privateService: AuthedRoutes[User, F] = uService

}

object UserServiceEndpoint {
  def apply[F[_]: Sync: Logger](
    userService: UserService[F]
  )(
    implicit F: ConcurrentEffect[F]
  ): UserServiceEndpoint[F] =
    new UserServiceEndpoint(
      userService
    )
}

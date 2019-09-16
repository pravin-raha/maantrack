package com.maantrack.endpoint

import cats.effect._
import cats.implicits._
import com.maantrack.domain.Error
import com.maantrack.domain.user._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe.generic.auto._
import io.circe.syntax._
import io.scalaland.chimney.dsl._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{ HttpRoutes, Response }
import tsec.authentication.{ TSecAuthService, _ }
import tsec.common.Verified
import tsec.passwordhashers.{ PasswordHash, PasswordHasher }

class UserServiceEndpoint[F[_]: Sync, A](
  bearerTokenAuth: BearerTokenAuthenticator[F, Long, User],
  userService: UserService[F],
  hasher: PasswordHasher[F, A]
)(implicit F: ConcurrentEffect[F])
    extends Http4sDsl[F] {

  private val userCreateService = HttpRoutes.of[F] {
    case req @ POST -> Root =>
      val res: F[Response[F]] = for {
        userRequest <- req.as[UserRequest]
        hash        <- hasher.hashpw(userRequest.password.getBytes)
        userRes <- userService
                    .addUser(userRequest.copy(password = hash))
                    .map(_.into[UserResponse].transform)
        result <- Ok(userRes.asJson)
      } yield result

      res.recoverWith {
        case e =>
          for {
            logger <- Slf4jLogger.create[F]
            _      <- logger.error(e)(e.getMessage)
            b      <- BadRequest()
          } yield b
      }
  }

  private val uService: AuthService[F] = TSecAuthService {
    case GET -> Root / LongVar(userId) asAuthed _ =>
      userService
        .getUserById(userId)
        .value
        .flatMap {
          case Some(user) => Ok(user.into[UserResponse].transform.asJson)
          case None       => NotFound(s"User with user id $userId not found".asJson)
        }
    case DELETE -> Root / LongVar(userId) asAuthed _ =>
      userService
        .deleteUserById(userId)
        .value
        .flatMap {
          case Some(user) => Ok(user.into[UserResponse].transform.asJson)
          case None       => NotFound(s"User with user id $userId not found".asJson)
        }
  }

  private val loginService: HttpRoutes[F] = HttpRoutes.of[F] {

    case req @ POST -> Root / "login" =>
      val res: F[Response[F]] = for {
        userCredential <- req.as[UserCredential]
        user <- userService
                 .getUserByUserName(userCredential.userName)
                 .toRight(Error.NotFound(): Throwable)
                 .value
                 .flatMap(_.liftTo[F])
        hash   = PasswordHash[A](user.password)
        status <- hasher.checkpw(userCredential.password.getBytes, hash)
        resp <- if (status == Verified) Ok()
               else Sync[F].raiseError[Response[F]](Error.BadLogin())
        tok <- bearerTokenAuth.create(user.userId)
      } yield bearerTokenAuth.embed(resp, tok)

      res.recoverWith {
        case e =>
          for {
            logger <- Slf4jLogger.create[F]
            _      <- logger.error(e)(e.getMessage)
            b      <- BadRequest()
          } yield b
      }
  }

  val publicService: HttpRoutes[F]   = loginService <+> userCreateService
  val privateService: AuthService[F] = uService

}

object UserServiceEndpoint {
  def apply[F[_]: Async, A](
    bearerTokenAuth: BearerTokenAuthenticator[F, Long, User],
    userService: UserService[F],
    hasher: PasswordHasher[F, A]
  )(
    implicit F: ConcurrentEffect[F]
  ): UserServiceEndpoint[F, A] =
    new UserServiceEndpoint(
      bearerTokenAuth,
      userService,
      hasher
    )
}

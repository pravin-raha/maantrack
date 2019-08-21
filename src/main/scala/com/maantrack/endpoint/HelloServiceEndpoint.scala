package com.maantrack.endpoint

import cats.effect._
import cats.implicits._
import com.maantrack.domain.Error
import com.maantrack.domain.user.{ User, UserCredential, UserRequest, UserResponse, UserService }
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.dsl.Http4sDsl
import org.http4s.{ EntityDecoder, HttpRoutes, Response }
import tsec.authentication.{ TSecAuthService, TSecBearerToken, _ }
import tsec.common.Verified
import tsec.passwordhashers.{ PasswordHash, PasswordHasher }
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import io.scalaland.chimney.dsl._

class HelloServiceEndpoint[F[_]: Sync, A](
  bearerTokenAuth: BearerTokenAuthenticator[F, Long, User],
  userService: UserService[F],
  hasher: PasswordHasher[F, A]
)(implicit F: ConcurrentEffect[F])
    extends Http4sDsl[F] {

  implicit val orderDecoder: EntityDecoder[F, UserResponse] = jsonOf

  type AuthService = TSecAuthService[User, TSecBearerToken[Long], F]

  val authService1: AuthService = TSecAuthService {
    //Where user is the case class User above
    case _ @GET -> Root / "api" asAuthed _ =>
      /*
      Note: The request is of type: SecuredRequest, which carries:
      1. The request
      2. The Authenticator (i.e token)
      3. The identity (i.e in this case, User)
       */
      Ok()
  }

  val authedService2: AuthService = TSecAuthService {
    case GET -> Root / "api2" asAuthed _ =>
      Ok()
  }

  val liftedComposed: AuthService = authService1 <+> authedService2

  val helloService: HttpRoutes[F] = HttpRoutes.of[F] {

    case req @ POST -> Root / "user" =>
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

    case req @ POST -> Root / "login" =>
      val res: F[Response[F]] = for {
        userCredential <- req.as[UserCredential]
        user <- userService
                 .getUserByUserName(userCredential.userName)
                 .toRight(Error.NotFound(): Throwable)
                 .value
                 .flatMap(_.raiseOrPure[F])
        hash   = PasswordHash[A](user.password)
        status <- hasher.checkpw(userCredential.password.getBytes, hash)
        resp <- if (status == Verified) Ok()
               else Sync[F].raiseError[Response[F]](Error.BadLogin())
        tok <- bearerTokenAuth.create(user.usersId)
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

  def publicService: HttpRoutes[F] = helloService
  def privateService: AuthService  = liftedComposed

}

object HelloServiceEndpoint {
  def apply[F[_]: Async, A](
    bearerTokenAuth: BearerTokenAuthenticator[F, Long, User],
    userService: UserService[F],
    hasher: PasswordHasher[F, A]
  )(
    implicit F: ConcurrentEffect[F]
  ): HelloServiceEndpoint[F, A] =
    new HelloServiceEndpoint(
      bearerTokenAuth,
      userService,
      hasher
    )
}

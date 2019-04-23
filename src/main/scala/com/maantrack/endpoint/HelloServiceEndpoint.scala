package com.maantrack.endpoint

import cats.effect._
import cats.implicits._
import com.maantrack.domain.Error
import com.maantrack.domain.user.{
  User,
  UserCredential,
  UserRequest,
  UserService
}
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}
import tsec.authentication.{TSecAuthService, TSecBearerToken, _}
import tsec.common.Verified
import tsec.passwordhashers.{PasswordHash, PasswordHasher}

import scala.language.higherKinds

class HelloServiceEndpoint[F[_]: Sync, A](
    bearerTokenAuth: BearerTokenAuthenticator[F, Long, User],
    userService: UserService[F],
    hasher: PasswordHasher[F, A]
)(implicit F: ConcurrentEffect[F])
    extends Http4sDsl[F] {

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
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name.")

    case req @ POST -> Root / "user" =>
      val res: F[Response[F]] = for {
        userRequest <- req.as[UserRequest]
        _ <- userService.addUser(userRequest)
        result <- Ok()
      } yield result

      res.recoverWith { case _ => BadRequest() }

    case req @ POST -> Root / "login" =>
      val res: F[Response[F]] = for {
        userCredential <- req.as[UserCredential]
        user <- userService
          .getUserByUserName(userCredential.userName)
          .toRight(Error.NotFound(): Throwable)
          .value
          .flatMap(_.raiseOrPure[F])
        hash = PasswordHash[A](user.password)
        status <- hasher.checkpw(userCredential.password.getBytes, hash)
        resp <- if (status == Verified) Ok()
        else Sync[F].raiseError[Response[F]](Error.BadLogin())
        tok <- bearerTokenAuth.create(user.id)
      } yield bearerTokenAuth.embed(resp, tok)

      res.recoverWith { case _ => BadRequest() }
  }

  def publicService: HttpRoutes[F] = helloService
  def privateService: AuthService = liftedComposed

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

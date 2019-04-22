package com.maantrack.endpoint

import cats.effect._
import cats.implicits._
import com.maantrack.auth.{TokenBackingStore, UserBackingStore}
import com.maantrack.domain.Error
import com.maantrack.domain.user.{
  User,
  UserCredential,
  UserRequest,
  UserService
}
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}
import tsec.authentication.{
  BearerTokenAuthenticator,
  SecuredRequestHandler,
  TSecAuthService,
  TSecBearerToken,
  TSecTokenSettings,
  _
}
import tsec.common.Verified
import tsec.passwordhashers.jca.JCAPasswordPlatform
import tsec.passwordhashers.{PasswordHash, PasswordHasher}

import scala.concurrent.duration._
import scala.language.higherKinds

class HelloServiceEndpoint[F[_]: Sync, A](
    userBackingStore: UserBackingStore[F],
    tokenBackingStore: TokenBackingStore[F],
    userService: UserService[F],
    hasher: JCAPasswordPlatform[A]
)(implicit F: ConcurrentEffect[F], P: PasswordHasher[F, A])
    extends Http4sDsl[F] {

  type AuthService = TSecAuthService[User, TSecBearerToken[Int], F]

  val settings: TSecTokenSettings = TSecTokenSettings(
    expiryDuration = 10.minutes, //Absolute expiration time
    maxIdle = None
  )

  val bearerTokenAuth =
    BearerTokenAuthenticator(
      tokenBackingStore,
      userBackingStore,
      settings
    )

  val Auth: SecuredRequestHandler[F, Int, User, TSecBearerToken[Int]] =
    SecuredRequestHandler[F, Int, User, TSecBearerToken[Int]](bearerTokenAuth)

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

  val lifted: HttpRoutes[F] = Auth.liftService(authService1)
  val liftedComposed: HttpRoutes[F] =
    Auth.liftService(authService1 <+> authedService2)

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
        status <- hasher.checkpw[F](userCredential.password.getBytes, hash)
        resp <- if (status == Verified) Ok()
        else Sync[F].raiseError[Response[F]](Error.BadLogin())
        tok <- bearerTokenAuth.create(user.id)
      } yield bearerTokenAuth.embed(resp, tok)

      res.recoverWith { case _ => BadRequest() }
  }

  def service: HttpRoutes[F] = helloService <+> liftedComposed

}

object HelloServiceEndpoint {
  def apply[F[_]: Async, A](
      userBackingStore: UserBackingStore[F],
      tokenBackingStore: TokenBackingStore[F],
      userService: UserService[F],
      hasher: JCAPasswordPlatform[A]
  )(
      implicit F: ConcurrentEffect[F],
      P: PasswordHasher[F, A]
  ): HelloServiceEndpoint[F, A] =
    new HelloServiceEndpoint(
      userBackingStore,
      tokenBackingStore,
      userService,
      hasher
    )
}

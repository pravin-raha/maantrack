package com.maantrack

import cats.effect.{Async, ConcurrentEffect}
import cats.implicits._
import com.maantrack.auth.{TokenBackingStore, UserBackingStore}
import com.maantrack.domain.token.TokenService
import com.maantrack.domain.user.{User, UserService}
import com.maantrack.endpoint.HelloServiceEndpoint
import com.maantrack.repository.doobies.{
  TokenRepositoryInterpreter,
  UserRepositoryInterpreter
}
import doobie.hikari.HikariTransactor
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.HttpRoutes
import tsec.authentication.{
  BearerTokenAuthenticator,
  SecuredRequestHandler,
  TSecBearerToken,
  TSecTokenSettings
}
import tsec.passwordhashers.PasswordHasher

import scala.concurrent.duration._

class Module[F[_]: Async, A](
    xa: HikariTransactor[F],
    hasher: PasswordHasher[F, A]
)(
    implicit F: ConcurrentEffect[F]
) {

  implicit def unsafeLogger: SelfAwareStructuredLogger[F] =
    Slf4jLogger.unsafeCreate[F]

  private lazy val userRepoInterpreter: UserRepositoryInterpreter[F] =
    UserRepositoryInterpreter(xa = xa)
  private lazy val userService: UserService[F] = UserService(
    userRepoInterpreter
  )

  private lazy val userBackingStore: UserBackingStore[F] = UserBackingStore(
    userService
  )

  private lazy val tokenRepository = TokenRepositoryInterpreter(xa)
  private lazy val tokenService: TokenService[F] = TokenService(tokenRepository)
  private lazy val tokenBackingStore: TokenBackingStore[F] = TokenBackingStore(
    tokenService
  )

  val settings: TSecTokenSettings = TSecTokenSettings(
    expiryDuration = 10.minutes, //Absolute expiration time
    maxIdle = None
  )

  val bearerTokenAuth: BearerTokenAuthenticator[F, Long, User] =
    BearerTokenAuthenticator(
      tokenBackingStore,
      userBackingStore,
      settings
    )

  val Auth: SecuredRequestHandler[F, Long, User, TSecBearerToken[Long]] =
    SecuredRequestHandler[F, Long, User, TSecBearerToken[Long]](bearerTokenAuth)

  val helloEndpoint: HelloServiceEndpoint[F, A] =
    HelloServiceEndpoint(
      bearerTokenAuth,
      userService,
      hasher
    )

  val httpEndpoint: HttpRoutes[F] = helloEndpoint.publicService <+> Auth
    .liftService(helloEndpoint.privateService)

}

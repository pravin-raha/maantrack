package com.maantrack

import cats.effect.{ Blocker, ConcurrentEffect, ContextShift, Sync }
import cats.implicits._
import com.maantrack.auth.{ TokenBackingStore, UserBackingStore }
import com.maantrack.domain.board.BoardService
import com.maantrack.domain.card.CardService
import com.maantrack.domain.cardlist.CardListService
import com.maantrack.domain.token.TokenService
import com.maantrack.domain.user.{ User, UserService }
import com.maantrack.endpoint.{
  BoardServiceEndpoint,
  CardListServiceEndpoint,
  CardServiceEndpoint,
  SwaggerUIServiceEndpoint,
  UserServiceEndpoint
}
import com.maantrack.repository.doobies.{
  BoardRepositoryInterpreter,
  CardListRepositoryInterpreter,
  CardRepositoryInterpreter,
  TokenRepositoryInterpreter,
  UserRepositoryInterpreter
}
import doobie.hikari.HikariTransactor
import io.chrisdavenport.log4cats.Logger
import org.http4s.HttpRoutes
import tsec.authentication.{ BearerTokenAuthenticator, SecuredRequestHandler, TSecBearerToken, TSecTokenSettings }
import tsec.passwordhashers.PasswordHasher

import scala.concurrent.duration._

class Module[F[_]: Sync: Logger, A](
  xa: HikariTransactor[F],
  hasher: PasswordHasher[F, A],
  blocker: Blocker
)(
  implicit F: ConcurrentEffect[F],
  cs: ContextShift[F]
) {

  private lazy val userRepoInterpreter: UserRepositoryInterpreter[F] =
    UserRepositoryInterpreter(xa = xa)
  private lazy val userService: UserService[F] = UserService(
    userRepoInterpreter
  )

  private lazy val userBackingStore: UserBackingStore[F] = UserBackingStore(
    userService
  )

  private lazy val tokenRepository               = TokenRepositoryInterpreter(xa)
  private lazy val tokenService: TokenService[F] = TokenService(tokenRepository)
  private lazy val tokenBackingStore: TokenBackingStore[F] = TokenBackingStore(
    tokenService
  )

  private val settings: TSecTokenSettings = TSecTokenSettings(
    expiryDuration = 10.minutes, //Absolute expiration time
    maxIdle = None
  )

  private val bearerTokenAuth: BearerTokenAuthenticator[F, Long, User] =
    BearerTokenAuthenticator(
      tokenBackingStore,
      userBackingStore,
      settings
    )

  private val Auth: SecuredRequestHandler[F, Long, User, TSecBearerToken[Long]] =
    SecuredRequestHandler[F, Long, User, TSecBearerToken[Long]](bearerTokenAuth)

  private val helloEndpoint: UserServiceEndpoint[F, A] =
    UserServiceEndpoint(
      bearerTokenAuth,
      userService,
      hasher
    )

  val swaggerEndpoint: SwaggerUIServiceEndpoint[F] = SwaggerUIServiceEndpoint(blocker)

  private val boardRepository: BoardRepositoryInterpreter[F] = new BoardRepositoryInterpreter[F](xa)
  private val boardService: BoardService[F]                  = new BoardService[F](boardRepository)
  val boardServiceEndpoint: HttpRoutes[F]                    = Auth.liftService(new BoardServiceEndpoint[F](boardService).privateService)

  private lazy val listRepository: CardListRepositoryInterpreter[F] = new CardListRepositoryInterpreter[F](xa)
  private lazy val listService: CardListService[F]                  = new CardListService[F](listRepository)
  val listEndpoint: HttpRoutes[F]                                   = new CardListServiceEndpoint[F](listService, Auth).service

  private lazy val cardRepository: CardRepositoryInterpreter[F] = new CardRepositoryInterpreter[F](xa)
  private lazy val cardService: CardService[F]                  = new CardService[F](cardRepository)
  val cardEndpoint: HttpRoutes[F]                               = new CardServiceEndpoint[F](cardService, Auth).service

  val userEndpoint: HttpRoutes[F] = helloEndpoint.publicService <+> Auth
    .liftService(helloEndpoint.privateService)

}

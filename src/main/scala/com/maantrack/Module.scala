package com.maantrack

import cats.effect.{ ConcurrentEffect, Sync }
import cats.implicits._
import com.maantrack.auth.{ TokenBackingStore, UserBackingStore }
import com.maantrack.db.{ Decoders, Encoders }
import com.maantrack.domain.board.BoardService
import com.maantrack.domain.card.CardService
import com.maantrack.domain.cardlist.CardListService
import com.maantrack.domain.token.TokenService
import com.maantrack.domain.user.{ User, UserService }
import com.maantrack.endpoint.{
  BoardServiceEndpoint,
  CardListServiceEndpoint,
  CardServiceEndpoint,
  UserServiceEndpoint
}
import com.maantrack.repository.doobies.{
  BoardRepositoryInterpreter,
  CardListRepositoryInterpreter,
  CardRepositoryInterpreter,
  TokenRepositoryInterpreter,
  UserRepositoryInterpreter
}
import doobie.quill.DoobieContext
import doobie.quill.DoobieContext.Postgres
import doobie.util.transactor.Transactor
import io.chrisdavenport.log4cats.Logger
import io.getquill.SnakeCase
import org.http4s.HttpRoutes
import tsec.authentication.{ BearerTokenAuthenticator, SecuredRequestHandler, TSecBearerToken, TSecTokenSettings }
import tsec.passwordhashers.PasswordHasher

import scala.concurrent.duration._

class Module[F[_]: Sync: Logger: ConcurrentEffect, A](
  xa: Transactor[F],
  hasher: PasswordHasher[F, A]
) {
  private lazy val ctx: Postgres[SnakeCase] with Decoders with Encoders =
    new DoobieContext.Postgres[SnakeCase](SnakeCase) with Decoders with Encoders

  private lazy val userRepoInterpreter: UserRepositoryInterpreter[F] =
    UserRepositoryInterpreter(xa = xa, ctx)
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

  private val boardRepository: BoardRepositoryInterpreter[F] = new BoardRepositoryInterpreter[F](xa, ctx)
  private val boardService: BoardService[F]                  = new BoardService[F](boardRepository)
  val boardServiceEndpoint: HttpRoutes[F]                    = Auth.liftService(new BoardServiceEndpoint[F](boardService).privateService)

  private lazy val listRepository: CardListRepositoryInterpreter[F] = new CardListRepositoryInterpreter[F](xa, ctx)
  private lazy val listService: CardListService[F]                  = new CardListService[F](listRepository)
  val listEndpoint: HttpRoutes[F]                                   = new CardListServiceEndpoint[F](listService, Auth).service

  private lazy val cardRepository: CardRepositoryInterpreter[F] = new CardRepositoryInterpreter[F](xa, ctx)
  private lazy val cardService: CardService[F]                  = new CardService[F](cardRepository)
  val cardEndpoint: HttpRoutes[F]                               = new CardServiceEndpoint[F](cardService, Auth).service

  val userEndpoint: HttpRoutes[F] = helloEndpoint.publicService <+> Auth
    .liftService(helloEndpoint.privateService)
}

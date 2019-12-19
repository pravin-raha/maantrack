package com.maantrack

import cats.data.{ Kleisli, OptionT }
import cats.effect.{ ConcurrentEffect, Sync }
import cats.implicits._
import com.maantrack.db.{ Decoders, Encoders }
import com.maantrack.domain.board.BoardService
import com.maantrack.domain.card.CardService
import com.maantrack.domain.cardlist.CardListService
import com.maantrack.domain.user.{ User, UserRequest, UserService }
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
  UserRepositoryInterpreter
}
import doobie.quill.DoobieContext
import doobie.quill.DoobieContext.Postgres
import doobie.util.transactor.Transactor
import io.chrisdavenport.log4cats.Logger
import io.getquill.SnakeCase
import org.http4s.Credentials.Token
import org.http4s.{ AuthScheme, AuthedRoutes, HttpRoutes, Request }
import org.http4s.server.AuthMiddleware
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import pdi.jwt.{ Jwt, JwtAlgorithm }
import io.circe.parser._
import io.circe.generic.auto._

class Module[F[_]: Sync: Logger: ConcurrentEffect](
  xa: Transactor[F]
) extends Http4sDsl[F] {

  private lazy val ctx: Postgres[SnakeCase] with Decoders with Encoders =
    new DoobieContext.Postgres[SnakeCase](SnakeCase) with Decoders with Encoders

  case class AuthUser(id: Long, name: String)

  val authUser: Kleisli[F, Request[F], Either[String, User]] = Kleisli(
    req => {
      val maybeToken: Option[String] = req.headers.get(Authorization).collect {
        case Authorization(Token(AuthScheme.Bearer, token)) => token
      }
      maybeToken
        .fold("Bearer token not found".asLeft[User].pure[F]) { token =>
          Jwt
            .decode(token, "53cr3t", JwtAlgorithm.allHmac())
            .toEither
            .fold(_ => "Invalid access token".asLeft, _.asRight)
            .map(
              c =>
                decode[UserRequest](c.content)
                  .map(_.toUser)
                  .fold(_ => "Token parsing error".asLeft, _.asRight)
            )
            .flatten
            .pure[F]
        }
    }
  )

  val onFailure: AuthedRoutes[String, F] = Kleisli(req => OptionT.liftF(Forbidden(req.context)))

  val authMiddleware: AuthMiddleware[F, User] =
    AuthMiddleware(authUser, onFailure)

  private lazy val userRepoInterpreter: UserRepositoryInterpreter[F] =
    UserRepositoryInterpreter(xa = xa, ctx)
  private lazy val userService: UserService[F] = UserService(
    userRepoInterpreter
  )

  private val helloEndpoint: UserServiceEndpoint[F] =
    UserServiceEndpoint(
      userService
    )

  private val boardRepository: BoardRepositoryInterpreter[F] = new BoardRepositoryInterpreter[F](xa, ctx)
  private val boardService: BoardService[F]                  = new BoardService[F](boardRepository)
  val boardServiceEndpoint: HttpRoutes[F]                    = authMiddleware(new BoardServiceEndpoint[F](boardService).privateService)

  private lazy val listRepository: CardListRepositoryInterpreter[F] = new CardListRepositoryInterpreter[F](xa, ctx)
  private lazy val listService: CardListService[F]                  = new CardListService[F](listRepository)
  val listEndpoint: HttpRoutes[F]                                   = authMiddleware(new CardListServiceEndpoint[F](listService).privateService)

  private lazy val cardRepository: CardRepositoryInterpreter[F] = new CardRepositoryInterpreter[F](xa, ctx)
  private lazy val cardService: CardService[F]                  = new CardService[F](cardRepository)
  val cardEndpoint: HttpRoutes[F]                               = authMiddleware(new CardServiceEndpoint[F](cardService).authService)

  val userEndpoint: HttpRoutes[F] = helloEndpoint.publicService <+> authMiddleware(helloEndpoint.privateService)

}

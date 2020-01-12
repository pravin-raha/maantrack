package com.maantrack

import cats.data.{ Kleisli, OptionT }
import cats.effect.{ Blocker, ConcurrentEffect, ContextShift, Sync, Timer }
import cats.implicits._
import com.maantrack.config.JwtConfig
import com.maantrack.db.{ Decoders, Encoders }
import com.maantrack.domain.User
import com.maantrack.endpoint._
import com.maantrack.endpoint.auth.LoginRoutes
import com.maantrack.repository.doobies.interpreter.{
  BoardRepositoryInterpreter,
  CardListRepositoryInterpreter,
  CardRepositoryInterpreter,
  UserRepositoryInterpreter
}
import com.maantrack.service._
import doobie.quill.DoobieContext
import doobie.quill.DoobieContext.Postgres
import doobie.util.transactor.Transactor
import io.chrisdavenport.log4cats.Logger
import io.circe.generic.auto._
import io.circe.parser._
import io.getquill.SnakeCase
import org.http4s.Credentials.Token
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware
import org.http4s.server.middleware._
import org.http4s.syntax.all._
import pdi.jwt.{ Jwt, JwtAlgorithm }

import scala.concurrent.duration._

class Module[F[_]: Sync: Logger: ConcurrentEffect: Timer: ContextShift](
  xa: Transactor[F],
  blocker: Blocker,
  jwtConfig: JwtConfig
) extends Http4sDsl[F] {

  private lazy val ctx: Postgres[SnakeCase] with Decoders with Encoders =
    new DoobieContext.Postgres[SnakeCase](SnakeCase) with Decoders with Encoders

  case class AuthUser(id: Long, name: String)

  private val authUser: Kleisli[F, Request[F], Either[String, User]] = Kleisli(req => {
    req.headers
      .get(Authorization)
      .collect {
        case Authorization(Token(AuthScheme.Bearer, token)) => token
      }
      .fold("Bearer token not found".asLeft[User].pure[F]) { token =>
        Jwt
          .decode(token, jwtConfig.hmacSecret, JwtAlgorithm.allHmac())
          .toEither
          .fold(_ => "Invalid access token".asLeft, _.asRight)
          .map(c => decode[User](c.content).fold(_ => "Token parsing error".asLeft, _.asRight))
          .flatten
          .pure[F]
      }
  })

  private val onFailure: AuthedRoutes[String, F] = Kleisli(req => OptionT.liftF(Forbidden(req.context)))

  private val authMiddleware: AuthMiddleware[F, User] = AuthMiddleware(authUser, onFailure)

  private val userRepoInterpreter: UserRepositoryInterpreter[F] = UserRepositoryInterpreter(xa = xa, ctx)
  private val userService: UserService[F]                       = UserService(userRepoInterpreter)
  private val userRoute: HttpRoutes[F]                          = UserRoutes[F](userService).routes(authMiddleware)

  private val boardRepository: BoardRepositoryInterpreter[F] = new BoardRepositoryInterpreter[F](xa, ctx)
  private val boardService: BoardService[F]                  = new BoardService[F](boardRepository)
  private val boardServiceEndpoint: HttpRoutes[F]            = new BoardRoutes[F](boardService).routes(authMiddleware)

  private val listRepository: CardListRepositoryInterpreter[F] = new CardListRepositoryInterpreter[F](xa, ctx)
  private val listService: CardListService[F]                  = new CardListService[F](listRepository)
  private val listEndpoint: HttpRoutes[F]                      = new CardListRoutes[F](listService).routes(authMiddleware)

  private val cardRepository: CardRepositoryInterpreter[F] = new CardRepositoryInterpreter[F](xa, ctx)
  private val cardService: CardService[F]                  = new CardService[F](cardRepository)
  private val cardEndpoint: HttpRoutes[F]                  = new CardRoutes[F](cardService).routes(authMiddleware)

  private val authService: AuthService[F]  = new AuthService[F](userService, jwtConfig)
  private val loginEndpoint: HttpRoutes[F] = new LoginRoutes[F](authService).routes

  private val swaggerRoute = SwaggerUIRoutes(blocker).routes

  private val routes = swaggerRoute <+> userRoute <+> loginEndpoint <+> boardServiceEndpoint <+> listEndpoint <+> cardEndpoint

  private val middleware: HttpRoutes[F] => HttpRoutes[F] = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    } andThen { http: HttpRoutes[F] =>
      CORS(http, CORS.DefaultCORSConfig)
    } andThen { http: HttpRoutes[F] =>
      Timeout(60.seconds)(http)
    }
  }

  private val loggers: HttpApp[F] => HttpApp[F] = {
    { http: HttpApp[F] =>
      RequestLogger.httpApp(logHeaders = true, logBody = true)(http)
    } andThen { http: HttpApp[F] =>
      ResponseLogger.httpApp(logHeaders = true, logBody = true)(http)
    }
  }

  val httpApp: HttpApp[F] = loggers(middleware(routes).orNotFound)

}

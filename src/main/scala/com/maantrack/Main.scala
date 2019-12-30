package com.maantrack

import cats.Monad
import cats.effect._
import cats.implicits._
import com.maantrack.config.{ DatabaseConfig, ServerConfig }
import com.maantrack.endpoint.SwaggerUIServiceEndpoint
import doobie.util.ExecutionContexts
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.chrisdavenport.log4cats.{ Logger, SelfAwareStructuredLogger }
import org.http4s.HttpApp
import org.http4s.server.blaze._
import org.http4s.server.{ Router, Server => H4Server }
import org.http4s.syntax.all._
import pureconfig.ConfigSource
import tsec.passwordhashers.jca.BCrypt
import pureconfig.generic.auto._

object Main extends IOApp {
  implicit val unsafeLogger: SelfAwareStructuredLogger[IO] =
    Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] =
    HttpServer.stream[IO, BCrypt].use(_ => IO.never).as(ExitCode.Success)
}

object HttpServer {
  def stream[F[_]: ConcurrentEffect: ContextShift: Timer: Logger, A]: Resource[F, H4Server[F]] =
    for {
      serverConfig <- Resource.liftF(
                       Monad[F].pure(
                         ConfigSource.default.at("server").loadOrThrow[ServerConfig]
                       )
                     )
      dataBaseConfig <- Resource.liftF(
                         Monad[F].pure(ConfigSource.default.at("database").loadOrThrow[DatabaseConfig])
                       )
      connEc <- ExecutionContexts.fixedThreadPool[F](
                 dataBaseConfig.poolSize
               )
      blocker <- Blocker[F]
      xa      <- DatabaseConfig.dbTransactor(dataBaseConfig, connEc, blocker)
      ctx     = new Module(xa, BCrypt.syncPasswordHasher[F])
      _       <- Resource.liftF(DatabaseConfig.initializeDb(dataBaseConfig))
      server <- BlazeServerBuilder[F]
                 .bindHttp(serverConfig.port, serverConfig.host)
                 .withHttpApp(httpApp(ctx, blocker))
                 .resource
    } yield server

  def httpApp[F[_]: Sync: Logger: ConcurrentEffect: ContextShift, A](
    ctx: Module[F, A],
    blocker: Blocker
  ): HttpApp[F] = {
    Router(
      "/"      -> SwaggerUIServiceEndpoint(blocker).service,
      "/user"  -> ctx.userEndpoint,
      "/board" -> ctx.boardServiceEndpoint,
      "/list"  -> ctx.listEndpoint,
      "/card"  -> ctx.cardEndpoint
    ).orNotFound
  }
}

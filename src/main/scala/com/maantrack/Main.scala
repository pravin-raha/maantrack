package com.maantrack

import cats.effect._
import cats.syntax.all._
import com.maantrack.config.{ DatabaseConfig, JwtConfig, ServerConfig }
import doobie.util.ExecutionContexts
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.{ Logger, SelfAwareStructuredLogger }
import org.http4s.server.blaze._
import org.http4s.server.{ Server => H4Server }
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import scala.concurrent.ExecutionContext.global

object Main extends IOApp {
  implicit val unsafeLogger: SelfAwareStructuredLogger[IO] =
    Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] =
    HttpServer.stream[IO].use(_ => IO.never).as(ExitCode.Success)
}

object HttpServer {

  def stream[F[_]: ConcurrentEffect: ContextShift: Timer: Logger]: Resource[F, H4Server[F]] =
    for {
      serverConfig   <- Resource.liftF(ConfigSource.default.at("server").loadOrThrow[ServerConfig].pure[F])
      dataBaseConfig <- Resource.liftF(ConfigSource.default.at("database").loadOrThrow[DatabaseConfig].pure[F])
      jwtConfig      <- Resource.liftF(ConfigSource.default.at("jwt").loadOrThrow[JwtConfig].pure[F])
      connEc         <- ExecutionContexts.fixedThreadPool[F](dataBaseConfig.poolSize)
      blocker        <- Blocker[F]
      xa             <- DatabaseConfig.dbTransactor(dataBaseConfig, connEc, blocker)
      module         = new Module(xa, blocker, jwtConfig)
      _              <- Resource.liftF(DatabaseConfig.initializeDb(dataBaseConfig))
      server <- BlazeServerBuilder[F](global)
                 .bindHttp(serverConfig.port, serverConfig.host)
                 .withHttpApp(module.httpApp)
                 .resource
    } yield server

}

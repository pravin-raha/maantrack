package com.maantrack

import cats.Monad
import cats.effect._
import cats.implicits._
import com.maantrack.config.{ DatabaseConfig, ServerConfig }
import doobie.util.ExecutionContexts
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.HttpApp
import org.http4s.server.blaze._
import org.http4s.server.{ Router, Server => H4Server }
import org.http4s.syntax.all._
import pureconfig.loadConfigOrThrow
import tsec.passwordhashers.jca.BCrypt
import pureconfig.generic.auto._

object Main extends IOApp {

  implicit def unsafeLogger: SelfAwareStructuredLogger[IO] =
    Slf4jLogger.unsafeCreate[IO]

  override def run(args: List[String]): IO[ExitCode] =
    HttpServer.stream[IO, BCrypt].use(_ => IO.never).as(ExitCode.Success)
}

object HttpServer {

  def stream[F[_]: ConcurrentEffect: ContextShift: Timer, A]: Resource[F, H4Server[F]] =
    for {
      serverConfig <- Resource.liftF(
                       Monad[F].pure(
                         loadConfigOrThrow[ServerConfig](namespace = "server")
                       )
                     )
      dataBaseConfig <- Resource.liftF(
                         Monad[F].pure(loadConfigOrThrow[DatabaseConfig]("database"))
                       )
      connEc <- ExecutionContexts.fixedThreadPool[F](
                 dataBaseConfig.poolSize
               )
      blocker <- Blocker[F]
      xa      <- DatabaseConfig.dbTransactor(dataBaseConfig, connEc, blocker)

      ctx = new Module(xa, BCrypt.syncPasswordHasher[F])
      _   <- Resource.liftF(DatabaseConfig.initializeDb(dataBaseConfig))
      server <- BlazeServerBuilder[F]
                 .bindHttp(serverConfig.port, serverConfig.host)
                 .withHttpApp(httpApp(ctx))
                 .resource
    } yield server

  def httpApp[F[_]: Async, A](ctx: Module[F, A]): HttpApp[F] = {
    Router(
      "/" -> ctx.httpEndpoint
    ).orNotFound
  }

}

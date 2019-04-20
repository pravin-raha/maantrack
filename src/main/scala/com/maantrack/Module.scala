package com.maantrack

import cats.effect.{Async, ConcurrentEffect, ContextShift, Timer}
import com.maantrack.endpoint.HelloServiceEndpoint
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.HttpRoutes
import org.http4s.client.Client

class Module[F[_]: Async](client: Client[F])(
    implicit F: ConcurrentEffect[F],
    CS: ContextShift[F],
    T: Timer[F]
) {

  implicit def unsafeLogger: SelfAwareStructuredLogger[F] =
    Slf4jLogger.unsafeCreate[F]

  val helloWorldService: HttpRoutes[F] = HelloServiceEndpoint[F].service
  val httpEndpoint: HttpRoutes[F] = helloWorldService

}


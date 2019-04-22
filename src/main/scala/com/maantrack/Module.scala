package com.maantrack

import cats.effect.{Async, ConcurrentEffect}
import com.maantrack.endpoint.HelloServiceEndpoint
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.HttpRoutes

class Module[F[_]: Async]()(
    implicit F: ConcurrentEffect[F]
) {

  implicit def unsafeLogger: SelfAwareStructuredLogger[F] =
    Slf4jLogger.unsafeCreate[F]

//  val helloWorldService: HttpRoutes[F] = HelloServiceEndpoint[F].service
  val httpEndpoint: HttpRoutes[F] = ???

}

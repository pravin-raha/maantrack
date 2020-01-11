package com.maantrack.endpoint.auth

import cats.effect.Sync
import cats.implicits._
import com.maantrack.domain.User
import com.maantrack.service.AuthService
import dev.profunktor.auth.AuthHeaders
import io.chrisdavenport.log4cats.Logger
import org.http4s.AuthedRoutes
import org.http4s.dsl.Http4sDsl

class LogoutRoutes[F[_]: Sync: Logger](
  authService: AuthService[F]
) extends Http4sDsl[F] {

  private val httpRoutes: AuthedRoutes[User, F] = AuthedRoutes.of {

    case ar @ POST -> Root / "logout" as user =>
      AuthHeaders
        .getBearerToken(ar.req)
        .traverse_(t => authService.logout(t, user.userName)) *> NoContent()

  }

  val privateService: AuthedRoutes[User, F] = httpRoutes
}

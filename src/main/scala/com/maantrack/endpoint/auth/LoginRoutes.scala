package com.maantrack.endpoint.auth

import cats.effect.Sync
import cats.implicits._
import com.maantrack.domain.{ InvalidUserOrPassword, UserCredential }
import com.maantrack.endpoint.decoder._
import com.maantrack.service.AuthService
import dev.profunktor.auth.jwt.JwtToken
import io.chrisdavenport.log4cats.Logger
import io.circe.{ Decoder, Encoder }
import org.http4s.circe.{ jsonEncoderOf, jsonOf }
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.{ EntityDecoder, EntityEncoder, HttpRoutes }

class LoginRoutes[F[_]: Sync: Logger](
  authService: AuthService[F]
) extends Http4sDsl[F] {

  implicit val tokenEncoder: Encoder[JwtToken] =
    Encoder.forProduct1("access_token")(_.value)

  implicit def jsonDecoder[A: Decoder]: EntityDecoder[F, A] = jsonOf[F, A]
  implicit def jsonEncoder[A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {

    case req @ POST -> Root / "login" =>
      req.decodeR[UserCredential] { user =>
        authService
          .login(user.userName, user.password)
          .flatMap(Ok(_))
          .handleErrorWith {
            case InvalidUserOrPassword(_) => Forbidden()
          }
      }

  }

  private val prefixPath = "/auth"

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
